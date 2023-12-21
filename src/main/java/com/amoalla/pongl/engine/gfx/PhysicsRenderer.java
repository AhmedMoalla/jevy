package com.amoalla.pongl.engine.gfx;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.PulleyJoint;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

public class PhysicsRenderer {

    private final Array<Body> bodies = new Array<>();
    private final static Array<Joint> joints = new Array<Joint>();

    private final Vector2[] vertices = new Vector2[1000];
    private int width;
    private int height;

    private boolean drawBodies;
    private boolean drawJoints;
    private boolean drawAABBs;
    private boolean drawInactiveBodies;
    private boolean drawVelocities;
    private boolean drawContacts;

    public PhysicsRenderer() {
        this(true, true, false, true, false, true);
    }

    public PhysicsRenderer(boolean drawBodies, boolean drawJoints, boolean drawAABBs, boolean drawInactiveBodies,
                           boolean drawVelocities, boolean drawContacts) {
        for (int i = 0; i < vertices.length; i++)
            vertices[i] = new Vector2();

        this.drawBodies = drawBodies;
        this.drawJoints = drawJoints;
        this.drawAABBs = drawAABBs;
        this.drawInactiveBodies = drawInactiveBodies;
        this.drawVelocities = drawVelocities;
        this.drawContacts = drawContacts;
    }

    public void render(World world, Renderer renderer) {
        if (drawBodies || drawAABBs) {
            world.getBodies(bodies);
            for (Body body : bodies) {
                if (body.isActive() || drawInactiveBodies) {
                    renderBody(renderer, body);
                }
            }
        }

        if (drawJoints) {
            world.getJoints(joints);
            for (Joint joint : joints) {
                drawJoint(renderer, joint);
            }
        }

        if (drawContacts) {
            for (Contact contact : world.getContactList()) {
                drawContact(renderer, contact);
            }
        }
    }

    private void drawContact(Renderer renderer, Contact contact) {
        WorldManifold worldManifold = contact.getWorldManifold();
        if (worldManifold.getNumberOfContactPoints() == 0) return;
        Vector2 point = worldManifold.getPoints()[0];
        Color color = getColorByBody(contact.getFixtureA().getBody());
        renderer.drawPoint(point.x, point.y, color);
    }

    private void drawJoint(Renderer renderer, Joint joint) {
        Body bodyA = joint.getBodyA();
        Body bodyB = joint.getBodyB();
        Transform xf1 = bodyA.getTransform();
        Transform xf2 = bodyB.getTransform();

        Vector2 x1 = xf1.getPosition();
        Vector2 x2 = xf2.getPosition();
        Vector2 p1 = joint.getAnchorA();
        Vector2 p2 = joint.getAnchorB();

        if (joint.getType() == JointDef.JointType.DistanceJoint) {
            drawSegment(renderer, p1, p2, JOINT_COLOR);
        } else if (joint.getType() == JointDef.JointType.PulleyJoint) {
            PulleyJoint pulley = (PulleyJoint) joint;
            Vector2 s1 = pulley.getGroundAnchorA();
            Vector2 s2 = pulley.getGroundAnchorB();
            drawSegment(renderer, s1, p1, JOINT_COLOR);
            drawSegment(renderer, s2, p2, JOINT_COLOR);
            drawSegment(renderer, s1, s2, JOINT_COLOR);
        } else if (joint.getType() == JointDef.JointType.MouseJoint) {
            drawSegment(renderer, joint.getAnchorA(), joint.getAnchorB(), JOINT_COLOR);
        } else {
            drawSegment(renderer, x1, p1, JOINT_COLOR);
            drawSegment(renderer, p1, p2, JOINT_COLOR);
            drawSegment(renderer, x2, p2, JOINT_COLOR);
        }
    }

    private void renderBody(Renderer renderer, Body body) {
        Transform transform = body.getTransform();
        for (Fixture fixture : body.getFixtureList()) {
            if (drawBodies) {
                drawShape(renderer, fixture, transform, getColorByBody(body));
                if (drawVelocities) {
                    Vector2 position = body.getPosition();
                    drawSegment(renderer, position, body.getLinearVelocity().add(position), VELOCITY_COLOR);
                }
            }

            if (drawAABBs) {
                drawAABB(renderer, fixture, transform);
            }
        }
    }

    private void drawAABB(Renderer renderer, Fixture fixture, Transform transform) {
        throw new UnsupportedOperationException();
    }

    private void drawSegment(Renderer renderer, Vector2 p1, Vector2 p2, Color color) {
        renderer.drawLine(p1.x, p1.y, p2.x, p2.y, color);
    }

    private Vector2 t = new Vector2();
    private Vector2 axis = new Vector2();

    private void drawShape(Renderer renderer, Fixture fixture, Transform transform, Color color) {
        switch (fixture.getShape()) {
            case CircleShape circle -> {
                t.set(circle.getPosition());
                transform.mul(t);
                drawSolidCircle(renderer, t, circle.getRadius(), axis.set(transform.vals[Transform.COS], transform.vals[Transform.SIN]), color);
            }
            case EdgeShape edge -> {
                edge.getVertex1(vertices[0]);
                edge.getVertex2(vertices[1]);
                transform.mul(vertices[0]);
                transform.mul(vertices[1]);
                drawSolidPolygon(renderer, vertices, 2, color, true);
            }
            case PolygonShape polygon -> {
                int vertexCount = polygon.getVertexCount();
                for (int i = 0; i < vertexCount; i++) {
                    polygon.getVertex(i, vertices[i]);
                    transform.mul(vertices[i]);
                }
                drawSolidPolygon(renderer, vertices, vertexCount, color, true);
            }
            case ChainShape chain -> {
                int vertexCount = chain.getVertexCount();
                for (int i = 0; i < vertexCount; i++) {
                    chain.getVertex(i, vertices[i]);
                    transform.mul(vertices[i]);
                }
                drawSolidPolygon(renderer, vertices, vertexCount, color, false);
            }
            default -> throw new IllegalStateException(STR."Unexpected value: \{fixture.getType()}");
        }
    }

    private final Vector2 f = new Vector2();
    private final Vector2 v = new Vector2();
    private final Vector2 lv = new Vector2();

    // Todo: replace with a proper draw circle
    private void drawSolidCircle(Renderer renderer, Vector2 center, float radius, Vector2 axis, Color color) {
        float angle = 0;
        float angleInc = 2 * (float) Math.PI / 20;
        for (int i = 0; i < 20; i++, angle += angleInc) {
            v.set((float) Math.cos(angle) * radius + center.x, (float) Math.sin(angle) * radius + center.y);
            if (i == 0) {
                lv.set(v);
                f.set(v);
                continue;
            }
            renderer.drawLine(lv.x, lv.y, v.x, v.y, color);
            lv.set(v);
        }
        renderer.drawLine(f.x, f.y, lv.x, lv.y, color);
        renderer.drawLine(center.x, center.y, center.x + axis.x * radius, center.y + axis.y * radius, color);
    }

    private void drawSolidPolygon(Renderer renderer, Vector2[] vertices, int vertexCount, Color color, boolean closed) {
        lv.set(vertices[0]);
        f.set(vertices[0]);
        for (int i = 1; i < vertexCount; i++) {
            Vector2 v = vertices[i];
            renderer.drawLine(lv.x, lv.y, v.x, v.y, color);
            lv.set(v);
        }
        if (closed)
            renderer.drawLine(f.x, f.y, lv.x, lv.y, color);
    }

    public final Color SHAPE_NOT_ACTIVE = new Color(0.5f, 0.5f, 0.3f, 1);
    public final Color SHAPE_STATIC = new Color(0.5f, 0.9f, 0.5f, 1);
    public final Color SHAPE_KINEMATIC = new Color(0.5f, 0.5f, 0.9f, 1);
    public final Color SHAPE_NOT_AWAKE = new Color(0.6f, 0.6f, 0.6f, 1);
    public final Color SHAPE_AWAKE = new Color(0.9f, 0.7f, 0.7f, 1);
    public final Color JOINT_COLOR = new Color(0.5f, 0.8f, 0.8f, 1);
    public final Color AABB_COLOR = new Color(1.0f, 0, 1.0f, 1f);
    public final Color VELOCITY_COLOR = new Color(1.0f, 0, 0f, 1f);

    private Color getColorByBody(Body body) {
        if (!body.isActive())
            return SHAPE_NOT_ACTIVE;
        else if (body.getType() == BodyDef.BodyType.StaticBody)
            return SHAPE_STATIC;
        else if (body.getType() == BodyDef.BodyType.KinematicBody)
            return SHAPE_KINEMATIC;
        else if (!body.isAwake())
            return SHAPE_NOT_AWAKE;
        else
            return SHAPE_AWAKE;
    }

}
