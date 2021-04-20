/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package sandbox.test3d;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.pathfinding.maze.Maze;
import com.almasb.fxgl.scene3d.Cylinder;
import com.almasb.fxgl.scene3d.Model3D;
import com.almasb.fxgl.scene3d.Prism;
import com.almasb.fxgl.scene3d.Pyramid;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static com.almasb.fxgl.core.math.FXGLMath.cosDeg;
import static com.almasb.fxgl.core.math.FXGLMath.sinDeg;
import static com.almasb.fxgl.dsl.FXGL.*;
import static java.lang.Math.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class Test3D {

    Point3D dir = new Point3D(0, 0, 0);
    private Point3D next = new Point3D(0, 0, 0);

    private Group root = new Group();

    private Random random = new Random();

    public void start() {
        getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);

        entityBuilder()
                .view(root)
                .buildAndAttach();

        var light = new PointLight();
        light.setTranslateX(0);
        light.setTranslateY(0);
        light.setTranslateZ(-12);

        var light2 = new PointLight();
        light2.setTranslateX(22);
        light2.setTranslateY(0);
        light2.setTranslateZ(-12);

        var light3 = new PointLight();
        light3.setTranslateX(42);
        light3.setTranslateY(0);
        light3.setTranslateZ(-12);

        //root.getChildren().addAll(light, light2, light3);

        mazeFull();
    }


    private void mazeFull() {
        var scale = 20;

        var mat = new PhongMaterial();
        mat.setDiffuseMap(image("3d/ground.JPG"));
        mat.setBumpMap(image("3d/ground_norm.JPG"));

        Maze maze = new Maze(20, 20);
        maze.forEach(cell -> {

            if (cell.hasLeftWall()) {
                Cube cube = new Cube(Color.BLUE, 0.2 * scale, 1 * scale, 1 * scale);
                cube.setMaterial(mat);

                cube.setTranslateX(cell.getX() * scale);
                cube.setTranslateY(0);
                cube.setTranslateZ(cell.getY() * scale);

                root.getChildren().addAll(cube);

                delay += 0.1;
            }

            if (cell.hasTopWall()) {
                Cube cube = new Cube(Color.BLUE, 1 * scale, 1 * scale, 0.2 * scale);
                cube.setMaterial(mat);

                cube.setTranslateX((cell.getX() + 0.5) * scale);
                cube.setTranslateY(0);
                cube.setTranslateZ((cell.getY() - 0.5) * scale);

                root.getChildren().addAll(cube);

                delay += 0.1;
            }
        });

        // right
        Cube cubeR = new Cube(Color.BLUE, 0.2 * scale, 1 * scale, 1 * maze.getHeight() * scale);
        cubeR.setMaterial(mat);

        cubeR.setTranslateX(maze.getWidth() * scale);
        cubeR.setTranslateY(0);
        cubeR.setTranslateZ((cubeR.getDepth() / 2 - 0.5) * scale);

        root.getChildren().addAll(cubeR);

        // bot
        Cube cubeB = new Cube(Color.BLUE, 1 * maze.getWidth() * scale, 1 * scale, 0.2 * scale);

        cubeB.setTranslateX((cubeB.getWidth() / 2) * scale);
        cubeB.setTranslateY(0);
        cubeB.setTranslateZ((maze.getHeight() - 0.5) * scale);

        root.getChildren().addAll(cubeB);

        var groundMat = new PhongMaterial();
        groundMat.setDiffuseColor(Color.BLACK);

        Cube ground = new Cube(Color.ANTIQUEWHITE, maze.getWidth() * scale, 0.2 * scale, maze.getHeight() * scale);
        ground.setMaterial(groundMat);
        ground.setTranslateX((maze.getWidth() / 2) * scale);
        ground.setTranslateY((0.1 + 0.5) * scale);
        ground.setTranslateZ((maze.getHeight() / 2 - 0.5) * scale);

        root.getChildren().add(ground);

        var sunLight = new PointLight();
        sunLight.setTranslateX(-50);
        sunLight.setTranslateZ(-50);
        sunLight.setTranslateY(-45);
        sunLight.setConstantAttenuation(0.6);

        var sunLight2 = new PointLight();
        sunLight2.setTranslateX(maze.getWidth() * scale + 50);
        sunLight2.setTranslateZ(maze.getHeight() * scale + 50);
        sunLight2.setTranslateY(-45);
        sunLight2.setConstantAttenuation(0.6);

        //root.getChildren().addAll(sunLight, sunLight2);
    }

    private void cylinderCeption() {
        var c = new Cylinder(1, 1, 4);
        c.setMaterial(new PhongMaterial(Color.BLUE));

        var e = entityBuilder()
                .view(c)
                .with("numSplits", 7)
                .with("dist", 25)
                .buildAndAttach();

        runOnce(() -> {
            split(e);

        }, Duration.seconds(3));
    }

    private void split(Entity e) {
        int numSplits = e.getInt("numSplits");
        int dist = e.getInt("dist");

        if (numSplits == 1)
            return;

        double anglePerSplit = 360.0 / numSplits;

        var newNumSplits = numSplits - 1;
        var newDist = dist - 2;

        for (int i = 0; i < numSplits; i++) {
            var e2 = entityBuilder()
                    .at(e.getPosition3D())
                    .view(new Prism(1, 1, 2, 6))
                    .with("numSplits", newNumSplits)
                    .with("dist", newDist)
                    .buildAndAttach();

            var x = cosDeg(i * anglePerSplit) * dist;
            var z = sinDeg(i * anglePerSplit) * dist;

            animationBuilder()
                    .interpolator(Interpolators.BACK.EASE_OUT())
                    .translate(e2)
                    .from(e.getPosition3D())
                    .to(e.getPosition3D().add(x, 0, z))
                    .buildAndPlay();

            runOnce(() -> {
                split(e2);
            }, Duration.seconds(2));
        }
    }

    private void loader() {
        var model = getAssetLoader().loadModel3D("gifts/gifts.obj");
        var mat = new PhongMaterial();
        mat.setDiffuseColor(Color.WHITE);
        //mat.setDiffuseMap(image("brick.png"));
        //model.setMaterial(mat);

        model.setRotationAxis(Rotate.X_AXIS);
        model.setRotate(90);

        model.getVertices().forEach(v -> {
            var p = new Point2D(v.getX() - 0, v.getZ() - 0).normalize();

            var angle = p.angle(1, 0);

            //p = p.multiply(angle / 180.0 * (v.getY() < 0 ? 5 : 2));


            //if (angle > 70 && v.getY() < 15 && v.getX() != 0.0) {

                final double t = delayIndex * (2 * PI / 64.0);

//                var p = new Point2D(cos(t), sin(t)).normalize().multiply(1.5);

                //if (angle > 90) {

                animationBuilder()
                        .interpolator(Interpolators.EXPONENTIAL.EASE_IN_OUT())
                        .delay(Duration.seconds(delayIndex * 0.005 ))
                        .repeatInfinitely()
                        .autoReverse(true)
                        .animate(v.xProperty())
                        .from(v.getX())
                        .to(v.getX() + p.getX())
                        .buildAndPlay();

                animationBuilder()
                        .interpolator(Interpolators.EXPONENTIAL.EASE_IN_OUT())
                        .delay(Duration.seconds(delayIndex * 0.005 ))
                        .repeatInfinitely()
                        .autoReverse(true)
                        .animate(v.zProperty())
                        .from(v.getZ())
                        .to(v.getZ() + p.getY())
                        .buildAndPlay();
                //}

//                animationBuilder()
//                        .interpolator(Interpolators.QUADRATIC.EASE_OUT())
//                        .delay(Duration.seconds(delayIndex * 0.005))
//                        .repeatInfinitely()
//                        .autoReverse(true)
//                        .animate(v.yProperty())
//                        .from(v.getY() + 0.5)
//                        .to(v.getY() + 0.5 * 2 + FXGLMath.sinDeg(delayIndex / 512.0 * 360.0) * 0.25)
//                        .buildAndPlay();

                delayIndex++;
            //}
        });

        entityBuilder()
                //.view(new Sphere(0.2))
                .view(model)
                //.scale(15, 15)
                .buildAndAttach();
    }

    private void combined() {
        var combinedPyramid = new Model3D();
        var p1 = new Pyramid(1, 0, 2, 4);
        //p1.setMaterial(new PhongMaterial(Color.WHITE));
        var p2 = new Pyramid(1, 0, 2, 4);
        //p2.setMaterial(new PhongMaterial(Color.BLUE));

        p1.setTranslateY(-1);
        p2.setTranslateY(1);
        p2.setRotate(180);

        combinedPyramid.addMeshView(p1);
        combinedPyramid.addMeshView(p2);

        combinedPyramid.setMaterial(new PhongMaterial(Color.BLUE));

        animationBuilder()
                .interpolator(Interpolators.SMOOTH.EASE_IN_OUT())
                .delay(Duration.seconds(delayIndex * 0.1 ))
                .repeatInfinitely()
                .autoReverse(true)
                .rotate(combinedPyramid)
                .from(new Point3D(0, 0, 0))
                .to(new Point3D(180, 0, 0))
                .buildAndPlay();

        animationBuilder()
                .interpolator(Interpolators.SMOOTH.EASE_IN_OUT())
                .delay(Duration.seconds(delayIndex * 0.1 ))
                .repeatInfinitely()
                .autoReverse(true)
                .animate(((PhongMaterial) combinedPyramid.getMaterial()).diffuseColorProperty())
                .from(Color.WHITE)
                .to(Color.BLUE)
                .buildAndPlay();

//        animationBuilder()
//                .interpolator(Interpolators.SMOOTH.EASE_IN_OUT())
//                .delay(Duration.seconds(delayIndex * 0.1 ))
//                .repeatInfinitely()
//                .autoReverse(true)
//                .animate(((PhongMaterial) p2.getMaterial()).diffuseColorProperty())
//                .from(Color.BLUE)
//                .to(Color.WHITE)
//                .buildAndPlay();

        root.getChildren().add(combinedPyramid);
    }

    private void flower2() {
        var cylinder = new Prism(1, 1, 2, 512);
        cylinder.setMaterial(new PhongMaterial(Color.BLUE));

        cylinder.getVertices().forEach(v -> {
            var p = new Point2D(v.getX() - 0, v.getZ() - 0).normalize();

            var angle = p.angle(1, 0);

            p = p.multiply(angle / 180.0 * (v.getY() < 0 ? 5 : 2));


            if (angle > 70 && v.getY() < 15 && v.getX() != 0.0) {

                final double t = delayIndex * (2 * PI / 64.0);

//                run(() -> {
//
//                    var p = curveFunction(t);
//
//                    v.setX(p.getX());
//                    v.setZ(p.getY());
//
//                }, Duration.seconds(0.016));



//                var p = new Point2D(cos(t), sin(t)).normalize().multiply(1.5);

                //if (angle > 90) {

                    animationBuilder()
                            .interpolator(Interpolators.EXPONENTIAL.EASE_IN_OUT())
                            .delay(Duration.seconds(delayIndex * 6.5 / 256.0  ))
                            .repeatInfinitely()
                            .autoReverse(true)
                            .animate(v.xProperty())
                            .from(v.getX())
                            .to(p.getX())
                            .buildAndPlay();

                    animationBuilder()
                            .interpolator(Interpolators.EXPONENTIAL.EASE_IN_OUT())
                            .delay(Duration.seconds(delayIndex * 6.5 / 256.0 ))
                            .repeatInfinitely()
                            .autoReverse(true)
                            .animate(v.zProperty())
                            .from(v.getZ())
                            .to(p.getY())
                            .buildAndPlay();
                //}

                animationBuilder()
                        .interpolator(Interpolators.QUADRATIC.EASE_OUT())
                        .delay(Duration.seconds(delayIndex * 7 / 512.0))
                        .repeatInfinitely()
                        .autoReverse(true)
                        .animate(v.yProperty())
                        .from(v.getY() + 0.5)
                        .to(v.getY() + 0.5 * 2 + FXGLMath.sinDeg(delayIndex / 512.0 * 360.0) * 0.25)
                        .buildAndPlay();

                delayIndex++;
            }
        });


        var sphere = new Sphere(0.75);
        sphere.setMaterial(new PhongMaterial(Color.LIGHTGREEN));
        sphere.setTranslateX(0.2);
        sphere.setTranslateY(-1);

        var leftLeg = new Box(0.4, 4, 0.4);
        leftLeg.setTranslateX(0);
        leftLeg.setTranslateZ(-0.3);
        leftLeg.setTranslateY(2);

        var rightLeg = new Box(0.4, 4, 0.4);
        rightLeg.setTranslateX(0);
        rightLeg.setTranslateZ(+0.3);
        rightLeg.setTranslateY(2);

        root.getChildren().addAll(cylinder, sphere, leftLeg, rightLeg);
    }

    private void keyPad() {
        var cylinder = new Cylinder();
        cylinder.getVertices().forEach(v -> {
            if (v.getY() < 0) {
                if (v.getZ() > 0) {
                    v.setY(v.getY() - 1);
                } else {
                    v.setY(v.getY() + 1);
                }
            }
        });

        root.getChildren().add(cylinder);

        int btnIndex = 1;

        for (int z = 2; z >= 0; z--) {
            for (int x = 0; x < 3; x++) {
                var box = new Cube(Color.LIGHTGREEN, 0.1, 0.1, 0.1);
                box.setTranslateX(x * 0.14 - 0.1);
                box.setTranslateZ(z * 0.14 - 0.6);
                box.setTranslateY(-0.02);

                var mat = box.material;

                var btnIndexFinal = btnIndex;

                onKeyDown(KeyCode.valueOf("DIGIT" + btnIndex),() -> {
                    animationBuilder()
                            .duration(Duration.seconds(0.33))
                            .repeat(2)
                            .autoReverse(true)
                            .interpolator(Interpolators.EXPONENTIAL.EASE_IN())
                            .translate(box)
                            .from(new Point3D(box.getTranslateX(), -0.02, box.getTranslateZ()))
                            .to(new Point3D(box.getTranslateX(), -0.02 + 0.04, box.getTranslateZ()))
                            .buildAndPlay();

                    animationBuilder()
                            .duration(Duration.seconds(0.7))
                            .animate(mat.diffuseColorProperty())
                            .from(Color.YELLOW.brighter())
                            .to(Color.LIGHTGREEN)
                            .buildAndPlay();

                    debug("Pressed " + btnIndexFinal);
                });

                btnIndex++;

                root.getChildren().add(box);
            }
        }

        var box = new Cube(Color.LIGHTGREEN, 0.1, 0.1, 0.1);
        box.setTranslateX(1 * 0.14 - 0.1);
        box.setTranslateZ(-1 * 0.14 - 0.6);
        box.setTranslateY(-0.02);

        root.getChildren().add(box);
    }

    private int delayIndex = 0;

    private void flower() {
        var cylinder = new Prism(1, 1, 2, 256);
        cylinder.setMaterial(new PhongMaterial(Color.LIGHTGREEN));

        cylinder.getVertices().forEach(v -> {
            if (v.getY() < 0) {

                final double t = delayIndex * (2 * PI / 64.0);

//                run(() -> {
//
//                    var p = curveFunction(t);
//
//                    v.setX(p.getX());
//                    v.setZ(p.getY());
//
//                }, Duration.seconds(0.016));



//                var p = new Point2D(cos(t), sin(t)).normalize().multiply(1.5);
                var p = new Point2D(v.getX() - 0, v.getZ() - 0).normalize().multiply(2.5);

                var angle = p.angle(1, 0);

                //System.out.println(angle);

                //if (angle > 90) {

                    animationBuilder()
                            //.interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                            .delay(Duration.seconds(delayIndex * 5.5 / 64.0))
                            .repeatInfinitely()
                            .autoReverse(true)
                            .animate(v.xProperty())
                            .from(v.getX())
                            .to(v.getX() + p.getX())
                            .buildAndPlay();

                    animationBuilder()
                            //.interpolator(Interpolators.BACK.EASE_IN())
                            .delay(Duration.seconds(delayIndex * 5.5 / 64.0))
                            .repeatInfinitely()
                            .autoReverse(true)
                            .animate(v.zProperty())
                            .from(v.getZ())
                            .to(v.getZ() + p.getY())
                            .buildAndPlay();
                //}

                animationBuilder()
                        .interpolator(Interpolators.BOUNCE.EASE_OUT())
                        .delay(Duration.seconds(delayIndex * 8 / 64.0))
                        .repeatInfinitely()
                        .autoReverse(true)
                        .animate(v.yProperty())
                        .from(v.getY())
                        .to(v.getY() - 2)
                        .buildAndPlay();

                delayIndex++;
            }
        });
//
//        delayIndex = 0;
//
//        var pyramid = new Pyramid(1, 0, 2, 5);
//        pyramid.setMaterial(new PhongMaterial(Color.GREEN));
//        pyramid.setTranslateX(5);
//        pyramid.setTranslateY(-3);
//        pyramid.getVertices().forEach(v -> {
//            if (v.getY() > 0) {
//                var p = new Point2D(v.getX(), v.getZ()).normalize().multiply(0.15);
//
//                animationBuilder()
//                        .interpolator(Interpolators.BOUNCE.EASE_OUT())
//                        .delay(Duration.seconds(delayIndex * 2 / 5.0))
//                        .duration(Duration.seconds(2))
//                        .repeatInfinitely()
//                        .autoReverse(true)
//                        .animate(v.xProperty())
//                        .from(v.getX())
//                        .to(v.getX() - p.getX())
//                        .buildAndPlay();
//
//                animationBuilder()
//                        .interpolator(Interpolators.BOUNCE.EASE_OUT())
//                        .delay(Duration.seconds(delayIndex * 2 / 5.0))
//                        .duration(Duration.seconds(2))
//                        .repeatInfinitely()
//                        .autoReverse(true)
//                        .animate(v.zProperty())
//                        .from(v.getZ())
//                        .to(v.getZ() - p.getY())
//                        .buildAndPlay();
//
//                animationBuilder()
//                        .interpolator(Interpolators.QUADRATIC.EASE_OUT())
//                        .delay(Duration.seconds(delayIndex * 1 / 5.0))
//                        .repeatInfinitely()
//                        .autoReverse(true)
//                        .animate(v.yProperty())
//                        .from(v.getY())
//                        .to(v.getY() + 3)
//                        .buildAndPlay();
//
//                delayIndex++;
//            }
//        });
//
//        delayIndex = 0;
//
//        var torus = new Torus();
//        torus.setTranslateX(-5);
//        torus.setTranslateY(-6.6);
//        torus.setMaterial(new PhongMaterial(Color.RED));
//        torus.getVertices().forEach(v -> {
//            animationBuilder()
//                    .interpolator(Interpolators.BOUNCE.EASE_OUT())
//                    .delay(Duration.seconds(delayIndex * 2 / 64.0 / 64))
//                    .duration(Duration.seconds(2))
//                    .repeatInfinitely()
//                    .autoReverse(true)
//                    .animate(v.xProperty())
//                    .from(v.getX())
//                    .to(v.getX() + random(0.1, 0.2))
//                    .buildAndPlay();
//
//            animationBuilder()
//                    .interpolator(Interpolators.BOUNCE.EASE_OUT())
//                    .delay(Duration.seconds(delayIndex * 2 / 64.0 / 64))
//                    .duration(Duration.seconds(2))
//                    .repeatInfinitely()
//                    .autoReverse(true)
//                    .animate(v.zProperty())
//                    .from(v.getZ())
//                    .to(v.getZ() - 1.5)
//                    .buildAndPlay();
//
////            animationBuilder()
////                    .interpolator(Interpolators.QUADRATIC.EASE_OUT())
////                    .delay(Duration.seconds(delayIndex * 1 / 5.0))
////                    .repeatInfinitely()
////                    .autoReverse(true)
////                    .animate(v.yProperty())
////                    .from(v.getY())
////                    .to(v.getY() + 3)
////                    .buildAndPlay();
//
//            delayIndex++;
//        });

        root.getChildren().addAll(cylinder);
    }

    private void rotations() {
        for (int i = 0; i < 55; i++) {
            var c = new Cube(Color.LIGHTGREEN, 1, 1, 0.02);
            var mat = new PhongMaterial(Color.LIGHTGREEN);
            c.setMaterial(mat);
            c.setTranslateX(i - 25);

            animationBuilder()
                    .repeatInfinitely()
                    .duration(Duration.seconds(0.1 * 55))
                    .interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                    .delay(Duration.seconds(i * 0.1))
                    .rotate(c)
                    .from(new Point3D(0, 0, 0))
                    .to(new Point3D(360, 0, 0))
                    .buildAndPlay();

            animationBuilder()
                    .repeatInfinitely()
                    .duration(Duration.seconds(0.1 * 55 / 2.0))
                    .delay(Duration.seconds(i * 0.1))
                    .autoReverse(true)
                    .animate(mat.diffuseColorProperty())
                    .from(Color.LIGHTGREEN)
                    .to(Color.BLUE)
                    .buildAndPlay();

            root.getChildren().addAll(c);
        }
    }

    private void logo() {

        var points = List.of(
                // F
                new Point2D(2, 2),
                new Point2D(3, 2),
                new Point2D(4, 2),
                new Point2D(5, 2),
                new Point2D(2, 3),
                new Point2D(2, 4),
                new Point2D(2, 5),
                new Point2D(2, 6),
                new Point2D(3, 4),
                new Point2D(4, 4),

                // X
                new Point2D(7, 2),
                new Point2D(8, 3),
                new Point2D(9, 4),
                new Point2D(8, 5),
                new Point2D(7, 6),
                new Point2D(10, 3),
                new Point2D(11, 2),
                new Point2D(10, 5),
                new Point2D(11, 6),

                // G
                new Point2D(13, 2),
                new Point2D(13, 3),
                new Point2D(13, 4),
                new Point2D(13, 5),
                new Point2D(13, 6),
                new Point2D(14, 6),
                new Point2D(15, 6),
                new Point2D(16, 6),
                new Point2D(16, 5),
                new Point2D(16, 4),
                new Point2D(15, 4),
                new Point2D(14, 2),
                new Point2D(15, 2),
                new Point2D(16, 2),

                // L
                new Point2D(18, 2),
                new Point2D(18, 3),
                new Point2D(18, 4),
                new Point2D(18, 5),
                new Point2D(18, 6),
                new Point2D(19, 6),
                new Point2D(20, 6),
                new Point2D(21, 6)
        );

        Cube[][] cubes = new Cube[23][10];

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 23; x++) {
                Cube cube = new Cube(Color.BLUE);
                cube.set(new Point3D(x, y, 0));

                cubes[x][y] = cube;

                root.getChildren().add(cube);


                if (points.contains(new Point2D(x, y))) {
                    var mat = new PhongMaterial(Color.BLUE);
                    cube.setMaterial(mat);

                    var point = new Point3D(cube.getTranslateX(), cube.getTranslateY(), cube.getTranslateZ());

                    animationBuilder()
                            .delay(Duration.seconds(3))
                            .duration(Duration.seconds(4.66))
                            .interpolator(Interpolators.BOUNCE.EASE_OUT())
                            .translate(cube)
                            .from(point)
                            .to(point.add(0, 0, -1))
                            .buildAndPlay();

                    animationBuilder()
                            .delay(Duration.seconds(3))
                            .duration(Duration.seconds(3.66))
                            .interpolator(Interpolators.BOUNCE.EASE_OUT())
                            .animate(mat.diffuseColorProperty())
                            .from(Color.BLUE)
                            .to(Color.YELLOW)
                            .buildAndPlay();
                } else {
                    animationBuilder()
                            .delay(Duration.seconds(3))
                            .duration(Duration.seconds(3.66))
                            .interpolator(Interpolators.SMOOTH.EASE_IN())
                            .translate(cube)
                            .alongPath(new CubicCurve(
                                    x, y,
                                    x + random(-10.0, 10.0), y + random(-10.0, 10.0),
                                    x + random(-10.0, 10.0), y + random(-10.0, 10.0),
                                    x, y
                            ))
                            .buildAndPlay();
                }
            }
        }



//        points.forEach(p -> {
//            int x = (int) p.getX();
//            int y = (int) p.getY();
//
//
//        });
//
//        Cube wall = new Cube(Color.BLUE, 23, 10, 1);
//        wall.set(new Point3D(11, 4.5, -0.1));
//
//        root.getChildren().add(wall);

        var light = new PointLight();
        light.setTranslateX(12);
        light.setTranslateY(4.5);
        light.setTranslateZ(-12);

        var light2 = new PointLight();
        light2.setTranslateX(22);
        light2.setTranslateY(4.5);
        light2.setTranslateZ(-12);

        var light3 = new PointLight();
        light3.setTranslateX(32);
        light3.setTranslateY(4.5);
        light3.setTranslateZ(-12);

        root.getChildren().addAll(light, light3);
    }

    private void hypercube() {
        int mapSize = 5;



        var points = new ArrayDeque<Point3D>();

        for (int z = 0; z < mapSize; z++) {
            for (int y = 0; y < mapSize; y++) {
                for (int x = 0; x < mapSize; x++) {
                    var p = new Point3D(x, y, z);
                    points.add(p);


                    var groupCube = new Group();

                    for (int z1 = 0; z1 < 5; z1++) {
                        for (int y1 = 0; y1 < 5; y1++) {
                            for (int x1 = 0; x1 < 5; x1++) {
                                Cube cube = new Cube(Color.BLUEVIOLET, 0.2, 0.2, 0.2);
                                cube.set(new Point3D(x1 * 0.2, y1 * 0.2, z1 * 0.2));

                                groupCube.getChildren().add(cube);
                            }
                        }
                    }

                    groupCube.setTranslateX(x);
                    groupCube.setTranslateY(y);
                    groupCube.setTranslateZ(z);

                    root.getChildren().add(groupCube);

                    animationBuilder()
                            .onFinished(() -> {
                                animationBuilder()
                                        .onFinished(() -> {

                                            groupCube.getChildren().forEach(cube -> {
                                                var smallCubePoint = new Point3D(cube.getTranslateX(), cube.getTranslateY(), cube.getTranslateZ());

                                                animationBuilder()
                                                        .onFinished(() -> {
                                                            animationBuilder()
                                                                    .onFinished(() -> {
                                                                        animationBuilder()
                                                                                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                                                                                .translate(groupCube)
                                                                                .from(new Point3D(groupCube.getTranslateX(), groupCube.getTranslateY(), groupCube.getTranslateZ()))
                                                                                .to(p)
                                                                                .buildAndPlay();
                                                                    })
                                                                    .translate(cube)
                                                                    .from(new Point3D(cube.getTranslateX(), cube.getTranslateY(), cube.getTranslateZ()))
                                                                    .to(smallCubePoint)
                                                                    .buildAndPlay();
                                                        })
                                                        .interpolator(Interpolators.BOUNCE.EASE_OUT())
                                                        .translate(cube)
                                                        .from(smallCubePoint)
                                                        .to(smallCubePoint.add(random(-10.0, 10.0), random(-10.0, 10.0), random(-10.0, 10.0)))
                                                        .buildAndPlay();
                                            });

                                        })
                                        .rotate(groupCube)
                                        .from(new Point3D(0, 0, 0))
                                        .to(new Point3D(FXGLMath.randomBoolean() ? 360 : -360, FXGLMath.randomBoolean() ? 360 : -360, 0))
                                        .buildAndPlay();
                            })
                            .delay(Duration.seconds(3))
                            .interpolator(Interpolators.BOUNCE.EASE_IN())
                            .translate(groupCube)
                            .from(new Point3D(x, y, z))
                            .to(new Point3D(x, y, z).add(random(-10.0, 10.0), random(-10.0, 10.0), random(-10.0, 10.0)))
                            .buildAndPlay();
                }
            }
        }
    }

    private void noise() {
        Map<Point3D, Cube> lookup = new HashMap<>();

        int mapSize = 100;
        int mapSizeY = 20;

        double[][] elevation = new double[mapSize][mapSize];

        for (int y = 0; y < mapSize; y++) {
            for (int x = 0; x < mapSize; x++) {
                double nx = x * 1.0/mapSize - 0.5;
                double ny = y * 1.0/mapSize - 0.5;
                elevation[y][x] = FXGLMath.noise2D(nx * 2.96, ny * 2.96) * 0.5 + 0.5;
            }
        }

        for (int z = 0; z < mapSize; z++) {
            for (int y = 0; y < mapSizeY; y++) {
                for (int x = 0; x < mapSize; x++) {


                    double elevationValue = elevation[z][x] * mapSizeY;

                    double flippedY = mapSizeY - y;

                    if (flippedY > elevationValue) {
                        continue;
                    }

                    double nx = x * 1.0/mapSize - 0.5;
                    double ny = y * 1.0/mapSizeY - 0.5;
                    double nz = z * 1.0/mapSize - 0.5;

                    //var noise = FXGLMath.noise3D(nx*3.2, ny*3.2, nz*3.2) + 0.5 * FXGLMath.noise3D(nx*9.2, ny*9.2, nz*9.2);

                    Cube cube;

                    if (flippedY > 15) {
                        cube = new Cube(Color.WHITE);
                    } else if (flippedY > 10) {
                        cube = new Cube(Color.BROWN);
                    } else if (flippedY > 2) {
                        cube = new Cube(Color.GREEN);
                    } else {
                        cube = new Cube(Color.BLUE);
                    }

                    cube.setTranslateX(x);
                    cube.setTranslateY(y);
                    cube.setTranslateZ(z);

                    lookup.put(new Point3D(x, y, z), cube);
                    root.getChildren().addAll(cube);
                }
            }
        }

        System.out.println("Cube size: " + root.getChildren().size());

        var list = root.getChildren()
                .stream()
                .map(n -> (Cube)n)
                .collect(Collectors.toList());

        list.forEach(cube -> {
            boolean isSurrounded = getPoints(new Point3D(cube.getTranslateX(), cube.getTranslateY(), cube.getTranslateZ()))
                    .stream()
                    .allMatch(p -> lookup.containsKey(p));

            if (isSurrounded) {
                root.getChildren().remove(cube);
            }
        });

        System.out.println("Cube size: " + root.getChildren().size());
    }

    private void multiply() {
        List<Point3D> points = new ArrayList<>();
        points.add(Point3D.ZERO);
        points.add(Point3D.ZERO);
        points.add(Point3D.ZERO);
        points.add(Point3D.ZERO);

        run(() -> {
            var p1 = points.get(points.size() - 4);
            var p2 = points.get(points.size() - 3);
            var p3 = points.get(points.size() - 2);
            var p4 = points.get(points.size() - 1);

            List.of(
                    new Pair<>(p1, Color.RED),
                    new Pair<>(p2, Color.BLUE),
                    new Pair<>(p3, Color.YELLOW),
                    new Pair<>(p4, Color.GREEN)
            ).forEach(pair -> {
                var pInitial = pair.getKey();
                var color = pair.getValue();

                getPoints(pInitial).stream()
                        .filter(p -> !points.contains(p))
                        .findAny()
                        .ifPresent(p -> {
                            points.add(p);

                            var sphere = new Cube(Color.RED);
                            sphere.setMaterial(new PhongMaterial(color));

                            root.getChildren().add(sphere);

                            animationBuilder()
                                    .interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                                    .translate(sphere)
                                    .from(pInitial)
                                    .to(p)
                                    .buildAndPlay();
                        });
            });

        }, Duration.seconds(0.1));
    }

    private List<Point3D> getPoints(Point3D p) {
        int dist = 1;

        var list = new ArrayList<>(List.of(
                new Point3D(p.getX() + dist, p.getY(), p.getZ()),
                new Point3D(p.getX() - dist, p.getY(), p.getZ()),
                new Point3D(p.getX(), p.getY() + dist, p.getZ()),
                new Point3D(p.getX(), p.getY() - dist, p.getZ()),
                new Point3D(p.getX(), p.getY(), p.getZ() + dist),
                new Point3D(p.getX(), p.getY(), p.getZ() - dist)
        ));

        //Collections.shuffle(list);

        return list;
    }

    private void onePlus() {
        var sphere1 = new Sphere(1);
        sphere1.setMaterial(new PhongMaterial(Color.RED));
        sphere1.setTranslateY(-5);

        var sphere2 = new Sphere(1);
        sphere2.setMaterial(new PhongMaterial(Color.RED));
        sphere2.setTranslateY(-5);
        sphere2.setTranslateZ(5);


        root.getChildren().addAll(sphere1, sphere2);

        Cube ground = new Cube(Color.ANTIQUEWHITE, 50, 0.2, 50);
        ground.setTranslateX(50 / 2);
        ground.setTranslateY(0.1 + 0.5);
        ground.setTranslateZ(50 / 2 - 0.5);

        root.getChildren().add(ground);
    }

    private void maze() {
        Maze maze = new Maze(20, 20);
        maze.forEach(cell -> {

            if (cell.hasLeftWall()) {
                Cube cube = new Cube(Color.BLUE, 0.2, 1, 1);

                cube.setTranslateX(cell.getX());
                cube.setTranslateY(0);
                cube.setTranslateZ(cell.getY());

                root.getChildren().addAll(cube);

                animationBuilder()
                        .delay(Duration.seconds(delay))
                        .interpolator(Interpolators.BOUNCE.EASE_OUT())
                        .duration(Duration.seconds(1.02))
                        .animate(cube.depthProperty())
                        .from(0.0)
                        .to(1.0)
                        .buildAndPlay();

                animationBuilder()
                        .delay(Duration.seconds(delay))
                        .interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                        .duration(Duration.seconds(1.02))
                        .animate(cube.widthProperty())
                        .from(0.0)
                        .to(0.2)
                        .buildAndPlay();

                delay += 0.1;
            }

            if (cell.hasTopWall()) {
                Cube cube = new Cube(Color.BLUE, 1, 1, 0.2);

                cube.setTranslateX(cell.getX() + 0.5);
                cube.setTranslateY(0);
                cube.setTranslateZ(cell.getY() - 0.5);

                root.getChildren().addAll(cube);

                animationBuilder()
                        .delay(Duration.seconds(delay))
                        .interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                        .duration(Duration.seconds(1.02))
                        .animate(cube.depthProperty())
                        .from(0.0)
                        .to(0.2)
                        .buildAndPlay();

                animationBuilder()
                        .delay(Duration.seconds(delay))
                        .interpolator(Interpolators.BOUNCE.EASE_OUT())
                        .duration(Duration.seconds(1.02))
                        .animate(cube.widthProperty())
                        .from(0.0)
                        .to(1.0)
                        .buildAndPlay();

                delay += 0.1;
            }
        });

        // right
        Cube cubeR = new Cube(Color.BLUE, 0.2, 1, 1 * maze.getHeight());

        cubeR.setTranslateX(maze.getWidth());
        cubeR.setTranslateY(0);
        cubeR.setTranslateZ(cubeR.getDepth() / 2 - 0.5);

        root.getChildren().addAll(cubeR);

        // bot
        Cube cubeB = new Cube(Color.BLUE, 1 * maze.getWidth(), 1, 0.2);

        cubeB.setTranslateX(cubeB.getWidth() / 2);
        cubeB.setTranslateY(0);
        cubeB.setTranslateZ(maze.getHeight() - 0.5);

        root.getChildren().addAll(cubeB);

        animationBuilder()
                .delay(Duration.seconds(delay))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .duration(Duration.seconds(1.02))
                .animate(cubeR.depthProperty())
                .from(0.0)
                .to(cubeR.getDepth())
                .buildAndPlay();

        animationBuilder()
                .delay(Duration.seconds(delay))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .duration(Duration.seconds(1.02))
                .animate(cubeR.widthProperty())
                .from(0.0)
                .to(0.2)
                .buildAndPlay();

        animationBuilder()
                .delay(Duration.seconds(delay))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .duration(Duration.seconds(1.02))
                .animate(cubeB.depthProperty())
                .from(0.0)
                .to(0.2)
                .buildAndPlay();

        animationBuilder()
                .delay(Duration.seconds(delay))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .duration(Duration.seconds(1.02))
                .animate(cubeB.widthProperty())
                .from(0.0)
                .to(cubeB.getWidth())
                .buildAndPlay();





        Cube ground = new Cube(Color.ANTIQUEWHITE, maze.getWidth(), 0.2, maze.getHeight());
        ground.setTranslateX(maze.getWidth() / 2);
        ground.setTranslateY(0.1 + 0.5);
        ground.setTranslateZ(maze.getHeight() / 2 - 0.5);

        root.getChildren().add(ground);
    }

    private double delay = 3.0;

    private void grid() {

        final var cubes = new ArrayList<Cube>();

        for (int z = 10; z < 11; z++) {

            for (int x = -10; x < 10; x++) {
                for (int y = -10; y < 10; y++) {
                    Cube cube = new Cube(Color.BLUE);

                    cube.setTranslateX(x);
                    cube.setTranslateY(y);
                    cube.setTranslateZ(z);

                    cubes.add(cube);
                    root.getChildren().addAll(cube);




                }


            }
        }

        cubes.sort(Comparator.comparingDouble(o1 -> new Point3D(o1.getTranslateX(), o1.getTranslateY(), o1.getTranslateZ()).distance(new Point3D(0, 0, 10))));

        cubes.forEach(cube -> {
            animationBuilder()
                    .delay(Duration.seconds(delay))
                    //.delay(Duration.seconds(1.02 / 12 * (x + 10)))
                    .interpolator(Interpolators.SMOOTH.EASE_OUT())
                    .repeatInfinitely()
                    .autoReverse(true)
                    .duration(Duration.seconds(1.02))
                    .translate(cube)
                    .from(new Point3D(cube.getTranslateX(), cube.getTranslateY(), cube.getTranslateZ()))
                    .to(new Point3D(cube.getTranslateX(), cube.getTranslateY(), cube.getTranslateZ() + 1))
                    .buildAndPlay();

            delay += 0.05;
        });
    }

    private void butterfly() {
        final var cubes = new ArrayList<TSphere>();

        for (int i = 0; i < 150; i++) {
            TSphere cube = new TSphere();
//            cube.setWidth(0.5);
//            cube.setHeight(0.5);
//            cube.setDepth(0.5);
            cube.setTranslateX(0);
            cube.setTranslateY(0);
            cube.setTranslateZ(15);

            cube.t = i * 0.016 * 2;

            cube.setRadius(0.5);
            cube.setMaterial(new PhongMaterial(Color.BLUE));


            cubes.add(cube);
            root.getChildren().add(cube);
        }

        var r = new Rotate(0, Rotate.Y_AXIS);
        r.setPivotX(0);
        r.setPivotY(0);
        r.setPivotZ(15);

        animationBuilder()
                .duration(Duration.seconds(3))
                .repeatInfinitely()
                .animate(r.angleProperty())
                .from(0)
                .to(360)
                .buildAndPlay();

        root.getTransforms().add(r);


        run(() -> {
            cubes.forEach(c -> {
                c.t += 0.016 * 0.25;

                var p = curveFunction(c.t);

                c.setTranslateX(p.getX());
                c.setTranslateY(p.getY());
            });
        }, Duration.seconds(0.016));
    }

    private Point2D curveFunction(double t) {
        final var v = pow(E, cos(t)) - 2 * cos(4 * t) - pow(sin(t / 12), 5);

        double x = sin(t) * v;
        double y = cos(t) * v;

        return new Point2D(x, -y).multiply(1.5);
    }

    private void textures() {
        Cube cube = new Cube(Color.BLUE);
        cube.setTranslateX(-2);
        cube.setTranslateY(-16);
        cube.setTranslateZ(-11);

        cube.material.setDiffuseColor(Color.WHITE);
        cube.material.setDiffuseMap(image("3d/1_wall.jpg"));
        //cube.material.setBumpMap(image("3d/1_wall_norm.jpg"));

        Cube cube2 = new Cube(Color.BLUE);

        cube2.setTranslateY(-16);
        cube2.setTranslateZ(-11);

        cube2.material.setDiffuseColor(Color.WHITE);
        cube2.material.setDiffuseMap(image("3d/1_wall.jpg"));
        cube2.material.setBumpMap(image("3d/1_wall_norm.jpg"));
        cube2.material.setSpecularColor(Color.WHITE);
        cube2.material.setSpecularMap(image("3d/1_wall.jpg"));

        LightBase light = new PointLight();
        light.setTranslateZ(-12);

        Box lightBox = new Box(0.2, 0.2, 0.2);
        lightBox.translateXProperty().bind(light.translateXProperty());
        light.setTranslateZ(-12);

        animationBuilder()
                .repeatInfinitely()
                .autoReverse(true)
                .translate(light)
                .from(new Point2D(-7, 0))
                .to(new Point2D(7, 0))
                .buildAndPlay();

        root.getChildren().addAll(cube, cube2, lightBox, light);
    }

    private void noise3() {
        double delay = 0.0;

        for (int z = -6; z < 19; z++) {
            for (int y = -4; y < -3; y++) {

                Color color = Color.BLUE;

                for (int x = -20; x < 20; x++) {
                    Cube cube = new Cube(color);

                    cube.setTranslateX(x);
                    cube.setTranslateY(y);
                    cube.setTranslateZ(z);

                    root.getChildren().addAll(cube);

//                    animationBuilder()
//                            .delay(Duration.seconds(delay))
//                            .interpolator(Interpolators.SINE.EASE_OUT())
//                            .repeatInfinitely()
//                            .autoReverse(true)
//                            .duration(Duration.seconds(1.72))
//                            .scale(cube)
//                            .from(new Point2D(1, 1))
//                            .to(new Point2D(0.00, 0.00))
//                            .buildAndPlay();
//
//                    animationBuilder()
//                            .delay(Duration.seconds(delay))
//                            .interpolator(Interpolators.SINE.EASE_OUT())
//                            .repeatInfinitely()
//                            .autoReverse(true)
//                            .duration(Duration.seconds(1.72))
//                            .animate(cube.scaleZProperty())
//                            .from(1)
//                            .to(0.00)
//                            .buildAndPlay();

//                    animationBuilder()
//                            .delay(Duration.seconds(delay))
//                            .interpolator(Interpolators.BACK.EASE_OUT())
//                            .repeatInfinitely()
//                            //.autoReverse(true)
//                            .duration(Duration.seconds(1.72))
//                            .rotate(cube)
//                            .from(0)
//                            .to(-180)
//                            .buildAndPlay();

                    animationBuilder()
                            .delay(Duration.seconds(delay))
                            .interpolator(Interpolators.SMOOTH.EASE_OUT())
                            .repeatInfinitely()
                            .autoReverse(true)
                            .duration(Duration.seconds(1.02))
                            .translate(cube)
                            .from(new Point2D(cube.getTranslateX(), -4))
                            .to(new Point2D(cube.getTranslateX(), 0))
                            .buildAndPlay();

                    animationBuilder()
                            .delay(Duration.seconds(delay))
                            .interpolator(Interpolators.BACK.EASE_OUT())
                            .repeatInfinitely()
                            .autoReverse(true)
                            .duration(Duration.seconds(1.02))
                            .animate(cube.material.diffuseColorProperty())
                            .from(Color.BLUE)
                            .to(Color.DARKBLUE)
                            .buildAndPlay();

                    delay += 0.025;
                }


            }
        }
    }

    private void noise2() {
        for (int z = 0; z < 1; z++) {
            for (int y = -4; y < -3; y++) {
                for (int x = -10; x < 10; x++) {
                    Cube cube = new Cube(Color.BLUE);

                    cube.setTranslateX(x);
                    cube.setTranslateY(y);
                    cube.setTranslateZ(z);

                    root.getChildren().addAll(cube);

                    animationBuilder()
                            .delay(Duration.seconds(1.02 / 12 * (x + 10)))
                            .interpolator(Interpolators.BACK.EASE_IN())
                            .repeatInfinitely()
                            .autoReverse(true)
                            .duration(Duration.seconds(1.02))
                            .scale(cube)
                            .from(new Point2D(1, 1))
                            .to(new Point2D(0.05, 0.05))
                            .buildAndPlay();

                    animationBuilder()
                            .delay(Duration.seconds(1.02 / 12 * (x + 10)))
                            .interpolator(Interpolators.BACK.EASE_IN())
                            .repeatInfinitely()
                            .autoReverse(true)
                            .duration(Duration.seconds(1.02))
                            .animate(cube.scaleZProperty())
                            .from(1)
                            .to(0.05)
                            .buildAndPlay();

                    animationBuilder()
                            .delay(Duration.seconds(1.02 / 12 * (x + 10)))
                            .interpolator(Interpolators.QUARTIC.EASE_IN())
                            .repeatInfinitely()
                            //.autoReverse(true)
                            .duration(Duration.seconds(1.02))
                            .rotate(cube)
                            .from(0)
                            .to(360)
                            .buildAndPlay();
                }
            }
        }
    }

    private static class TSphere extends Sphere {
        private double t = 0.0;
    }

    private static class Cube extends Box {

        private double t = 0.0;
        private PhongMaterial material;

        public Cube(Color color, double w, double h, double d) {
            super(w, h, d);

            material = new PhongMaterial(color);
            setMaterial(material);
        }

        public Cube(Color color) {
            this(color, 1, 1, 1);
        }

        public void set(Point3D p) {
            setTranslateX(p.getX());
            setTranslateY(p.getY());
            setTranslateZ(p.getZ());
        }

        public boolean isColliding(Cube c) {
            return getBoundsInParent().intersects(c.getBoundsInParent());
        }
    }
}