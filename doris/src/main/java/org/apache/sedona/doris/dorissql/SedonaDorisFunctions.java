/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sedona.doris.dorissql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.sedona.common.Constructors;
import org.apache.sedona.common.Functions;
import org.apache.sedona.common.FunctionsGeoTools;
import org.apache.sedona.common.Predicates;
import org.apache.sedona.common.enums.FileDataSplitter;
import org.apache.sedona.common.sphere.Haversine;
import org.apache.sedona.common.sphere.Spheroid;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;
import org.xml.sax.SAXException;

public class SedonaDorisFunctions {

    public static class ST_Area extends UDF {
        public double evaluate(String geometry) throws Exception {
            return Functions.area(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Intersects extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.intersects(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Affine extends UDF {
        public String evaluate(String geometry, double a, double b, double c, double d, double e, double f, double g, double h, double i, double xOff, double yOff, double zOff) {
            return new String(GeometrySerde.serialize(Functions.affine(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), 
                    a, b, c, d, e, f, g, h, i, xOff, yOff, zOff)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double a, double b, double d, double e, double xOff, double yOff) {
            return new String(GeometrySerde.serialize(Functions.affine(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), 
                    a, b, d, e, xOff, yOff)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Angle extends UDF {
        public double evaluate(String geom1, String geom2) {
            return Functions.angle(
                    GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)));
        }

        public double evaluate(String geom1, String geom2, String geom3) {
            return Functions.angle(
                    GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom3.getBytes(StandardCharsets.UTF_8)));
        }

        public double evaluate(String geom1, String geom2, String geom3, String geom4) {
            return Functions.angle(
                    GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom3.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom4.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_3DDistance extends UDF {
        public double evaluate(String left, String right) {
            return Functions.distance3d(
                    GeometrySerde.deserialize(left.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(right.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_AddPoint extends UDF {
        public String evaluate(String linestring, String point, int position) {
            return new String(GeometrySerde.serialize(Functions.addPoint(
                    GeometrySerde.deserialize(linestring.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(point.getBytes(StandardCharsets.UTF_8)),
                    position)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_AreaSpheroid extends UDF {
        public double evaluate(String geometry) {
            return Spheroid.area(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_AsBinary extends UDF {
        public String evaluate(String geometry) {
            return new String(Functions.asWKB(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_AsEWKB extends UDF {
        public String evaluate(String geometry) {
            return new String(Functions.asEWKB(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_AsEWKT extends UDF {
        public String evaluate(String geometry) {
            return Functions.asEWKT(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_AsGML extends UDF {
        public String evaluate(String geometry) {
            return Functions.asGML(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_AsGeoJSON extends UDF {
        public String evaluate(String geometry) {
            return Functions.asGeoJson(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }

        public String evaluate(String geometry, String type) {
            return Functions.asGeoJson(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), type);
        }
    }

    public static class ST_AsHEXEWKB extends UDF {
        public String evaluate(String geometry) {
            return Functions.asHexEWKB(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }

        public String evaluate(String geometry, String endian) {
            return Functions.asHexEWKB(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), endian);
        }
    }

    public static class ST_AsKML extends UDF {
        public String evaluate(String geometry) {
            return Functions.asKML(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_AsText extends UDF {
        public String evaluate(String geometry) {
            return Functions.asWKT(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Azimuth extends UDF {
        public double evaluate(String left, String right) {
            return Functions.azimuth(
                    GeometrySerde.deserialize(left.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(right.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_BestSRID extends UDF {
        public int evaluate(String geometry) {
            return Functions.bestSRID(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Boundary extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.boundary(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_BoundingDiagonal extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.boundingDiagonal(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Buffer extends UDF {
        public String evaluate(String geometry, double radius) throws IllegalArgumentException {
            return new String(GeometrySerde.serialize(Functions.buffer(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), radius)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double radius, boolean useSpheroid) throws IllegalArgumentException {
            return new String(GeometrySerde.serialize(Functions.buffer(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), radius, useSpheroid)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double radius, boolean useSpheroid, String parameters) throws IllegalArgumentException {
            return new String(GeometrySerde.serialize(Functions.buffer(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), radius, useSpheroid, parameters)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_BuildArea extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.buildArea(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Centroid extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.getCentroid(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ClosestPoint extends UDF {
        public String evaluate(String geom1, String geom2) {
            return new String(GeometrySerde.serialize(Functions.closestPoint(
                    GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_CollectionExtract extends UDF {
        public String evaluate(String geometry) throws IOException {
            return new String(GeometrySerde.serialize(Functions.collectionExtract(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, int geomType) throws IOException {
            return new String(GeometrySerde.serialize(Functions.collectionExtract(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), geomType)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ConcaveHull extends UDF {
        public String evaluate(String geometry, double pctConvex) {
            return new String(GeometrySerde.serialize(Functions.concaveHull(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), pctConvex, false)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double pctConvex, boolean allowHoles) {
            return new String(GeometrySerde.serialize(Functions.concaveHull(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), pctConvex, allowHoles)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Contains extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.contains(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_ConvexHull extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.convexHull(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_CoordDim extends UDF {
        public int evaluate(String geometry) {
            return Functions.nDims(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_CoveredBy extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.coveredBy(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Covers extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.covers(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Crosses extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.crosses(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_CrossesDateLine extends UDF {
        public boolean evaluate(String geometry) {
            return Functions.crossesDateLine(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_DWithin extends UDF {
        public boolean evaluate(String geomA, String geomB, double distance) {
            return Predicates.dWithin(
                    GeometrySerde.deserialize(geomA.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geomB.getBytes(StandardCharsets.UTF_8)),
                    distance);
        }
    }

    public static class ST_Degrees extends UDF {
        public double evaluate(double angleInRadian) {
            return Functions.degrees(angleInRadian);
        }
    }

    public static class ST_DelaunayTriangles extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.delaunayTriangle(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double tolerance) {
            return new String(GeometrySerde.serialize(Functions.delaunayTriangle(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), tolerance)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double tolerance, int flag) {
            return new String(GeometrySerde.serialize(Functions.delaunayTriangle(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), tolerance, flag)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Difference extends UDF {
        public String evaluate(String leftGeometry, String rightGeometry) {
            return new String(GeometrySerde.serialize(Functions.difference(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Dimension extends UDF {
        public Integer evaluate(String geometry) {
            return Functions.dimension(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Disjoint extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.disjoint(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Distance extends UDF {
        public double evaluate(String left, String right) {
            return Functions.distance(
                    GeometrySerde.deserialize(left.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(right.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_DistanceSphere extends UDF {
        public Double evaluate(String geomA, String geomB) {
            return Haversine.distance(
                    GeometrySerde.deserialize(geomA.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geomB.getBytes(StandardCharsets.UTF_8)));
        }

        public Double evaluate(String geomA, String geomB, double radius) {
            return Haversine.distance(
                    GeometrySerde.deserialize(geomA.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geomB.getBytes(StandardCharsets.UTF_8)),
                    radius);
        }
    }

    public static class ST_DistanceSpheroid extends UDF {
        public Double evaluate(String geomA, String geomB) {
            return Spheroid.distance(
                    GeometrySerde.deserialize(geomA.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geomB.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_DumpPoints extends UDF {
        public String evaluate(String geometry) {
            Geometry[] points = Functions.dumpPoints(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
            return new String(GeometrySerde.serialize(
                    GeometrySerde.GEOMETRY_FACTORY.createMultiPoint((Point[]) points)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_EndPoint extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.endPoint(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Envelope extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.envelope(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Equals extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.equals(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Expand extends UDF {
        public String evaluate(String geometry, double uniformDelta) {
            return new String(GeometrySerde.serialize(Functions.expand(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), uniformDelta)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double deltaX, double deltaY) {
            return new String(GeometrySerde.serialize(Functions.expand(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), deltaX, deltaY)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double deltaX, double deltaY, double deltaZ) {
            return new String(GeometrySerde.serialize(Functions.expand(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), deltaX, deltaY, deltaZ)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ExteriorRing extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.exteriorRing(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_FlipCoordinates extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.flipCoordinates(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Force2D extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.force2D(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Force3D extends UDF {
        public String evaluate(String geom) {
            WKBWriter writer = new WKBWriter(3);
            return new String(GeometrySerde.serialize(
                    Functions.force3D(
                            GeometrySerde.deserialize(writer.write(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, double zValue) {
            WKBWriter writer = new WKBWriter(3);
            return new String(GeometrySerde.serialize(
                    Functions.force3D(
                            GeometrySerde.deserialize(writer.write(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))),
                            zValue)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Force3DZ extends UDF {
        public String evaluate(String geom) {
            WKBWriter writer = new WKBWriter(3);
            return new String(GeometrySerde.serialize(
                    Functions.force3D(
                            GeometrySerde.deserialize(writer.write(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, double zValue) {
            WKBWriter writer = new WKBWriter(3);
            return new String(GeometrySerde.serialize(
                    Functions.force3D(
                            GeometrySerde.deserialize(writer.write(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))),
                            zValue)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ForceCollection extends UDF {
        public String evaluate(String geom) {
            return new String(GeometrySerde.serialize(Functions.forceCollection(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ForcePolygonCCW extends UDF {
        public String evaluate(String geom) {
            return new String(GeometrySerde.serialize(Functions.forcePolygonCCW(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ForcePolygonCW extends UDF {
        public String evaluate(String geom) {
            return new String(GeometrySerde.serialize(Functions.forcePolygonCW(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ForceRHR extends UDF {
        public String evaluate(String geom) {
            return new String(GeometrySerde.serialize(Functions.forcePolygonCW(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Force_2D extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.force2D(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_FrechetDistance extends UDF {
        public double evaluate(String geomA, String geomB) {
            return Functions.frechetDistance(
                    GeometrySerde.deserialize(geomA.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geomB.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_GeneratePoints extends UDF {
        public String evaluate(String geometry, int numPoints) {
            return new String(GeometrySerde.serialize(
                    Functions.generatePoints(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), numPoints)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, int numPoints, long seed) {
            return new String(GeometrySerde.serialize(
                    Functions.generatePoints(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), numPoints, seed)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeoHash extends UDF {
        public String evaluate(String geometry, int precision) {
            return Functions.geohash(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), precision);
        }
    }

    public static class ST_GeomCollFromText extends UDF {
        public String evaluate(String wkt) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.geomCollFromText(wkt, 0)), StandardCharsets.UTF_8);
        }

        public String evaluate(String wkt, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.geomCollFromText(wkt, srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeomFromEWKB extends UDF {
        public String evaluate(String wkb) throws ParseException {
            return wkb;
        }
    }

    public static class ST_GeomFromGML extends UDF {
        public String evaluate(String gml) throws IOException, ParserConfigurationException, SAXException {
            return new String(GeometrySerde.serialize(Constructors.geomFromGML(gml)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeomFromGeoHash extends UDF {
        public String evaluate(String geoHash, Integer precision) {
            return new String(GeometrySerde.serialize(Constructors.geomFromGeoHash(geoHash, precision)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeomFromGeoJSON extends UDF {
        public String evaluate(String geoJson) {
            return new String(GeometrySerde.serialize(Constructors.geomFromText(geoJson, FileDataSplitter.GEOJSON)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeomFromKML extends UDF {
        public String evaluate(String kml) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.geomFromKML(kml)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeomFromText extends UDF {
        public String evaluate(String geomString) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.geomFromWKT(geomString, 0)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geomString, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.geomFromWKT(geomString, srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeomFromWKB extends UDF {
        public String evaluate(String wkb) throws ParseException {
            return wkb;
        }
    }

    public static class ST_GeomFromWKT extends UDF {
        public String evaluate(String wkt) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.geomFromWKT(wkt, 0)), StandardCharsets.UTF_8);
        }

        public String evaluate(String wkt, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.geomFromWKT(wkt, srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeometricMedian extends UDF {
        public String evaluate(String geom) throws Exception {
            return new String(GeometrySerde.serialize(Functions.geometricMedian(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, float tolerance) throws Exception {
            return new String(GeometrySerde.serialize(
                    Functions.geometricMedian(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), tolerance)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, float tolerance, int maxIter) throws Exception {
            return new String(GeometrySerde.serialize(
                    Functions.geometricMedian(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), tolerance, maxIter)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, float tolerance, int maxIter, boolean failIfNotConverged) throws Exception {
            return new String(GeometrySerde.serialize(
                    Functions.geometricMedian(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), tolerance, maxIter, failIfNotConverged)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeometryFromText extends UDF {
        public String evaluate(String geomString) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.geomFromWKT(geomString, 0)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geomString, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.geomFromWKT(geomString, srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeometryN extends UDF {
        public String evaluate(String geometry, int n) {
            return new String(GeometrySerde.serialize(Functions.geometryN(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), n)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_GeometryType extends UDF {
        public String evaluate(String geometry) {
            return Functions.geometryType(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_HasZ extends UDF {
        public boolean evaluate(String geometry) {
            return Functions.hasZ(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_HausdorffDistance extends UDF {
        public double evaluate(String geom1, String geom2) {
            return Functions.hausdorffDistance(
                    GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)));
        }

        public double evaluate(String geom1, String geom2, double densifyFrac) {
            return Functions.hausdorffDistance(
                    GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)),
                    densifyFrac);
        }
    }

    public static class ST_InteriorRingN extends UDF {
        public String evaluate(String geometry, int n) {
            return new String(GeometrySerde.serialize(Functions.interiorRingN(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), n)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Intersection extends UDF {
        public String evaluate(String leftGeometry, String rightGeometry) {
            return new String(GeometrySerde.serialize(Functions.intersection(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_IsClosed extends UDF {
        public boolean evaluate(String geometry) {
            return Functions.isClosed(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_IsCollection extends UDF {
        public boolean evaluate(String geometry) {
            return Functions.isCollection(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_IsEmpty extends UDF {
        public boolean evaluate(String geometry) {
            return Functions.isEmpty(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_IsPolygonCCW extends UDF {
        public boolean evaluate(String geom) {
            return Functions.isPolygonCCW(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_IsPolygonCW extends UDF {
        public boolean evaluate(String geom) {
            return Functions.isPolygonCW(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_IsRing extends UDF {
        public boolean evaluate(String geometry) {
            return Functions.isRing(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_IsSimple extends UDF {
        public boolean evaluate(String geometry) {
            return Functions.isSimple(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_IsValid extends UDF {
        public boolean evaluate(String geometry) {
            return Functions.isValid(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }

        public boolean evaluate(String geometry, int flags) {
            return Functions.isValid(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), flags);
        }
    }

    public static class ST_IsValidReason extends UDF {
        public String evaluate(String geometry) {
            return Functions.isValidReason(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }

        public String evaluate(String geometry, int flags) {
            return Functions.isValidReason(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), flags);
        }
    }

    public static class ST_LabelPoint extends UDF {
        public String evaluate(String geom) {
            return new String(GeometrySerde.serialize(Functions.labelPoint(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, int gridResolution) {
            return new String(GeometrySerde.serialize(
                    Functions.labelPoint(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), gridResolution)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, int gridResolution, double goodnessThreshold) {
            return new String(GeometrySerde.serialize(
                    Functions.labelPoint(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), gridResolution, goodnessThreshold)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Length extends UDF {
        public double evaluate(String geometry) {
            return Functions.length(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Length2D extends UDF {
        public double evaluate(String geometry) {
            return Functions.length(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_LengthSpheroid extends UDF {
        public double evaluate(String geom) {
            return Spheroid.length(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_LineFromMultiPoint extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(
                    Functions.lineFromMultiPoint(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_LineFromText extends UDF {
        public String evaluate(String geomString) {
            return new String(GeometrySerde.serialize(Constructors.lineFromText(geomString)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_LineFromWKB extends UDF {
        public String evaluate(String wkb) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.lineFromWKB(wkb.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }

        public String evaluate(String wkb, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.lineFromWKB(wkb.getBytes(StandardCharsets.UTF_8), srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_LineInterpolatePoint extends UDF {
        public String evaluate(String geom, double fraction) {
            return new String(GeometrySerde.serialize(
                    Functions.lineInterpolatePoint(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), fraction)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_LineLocatePoint extends UDF {
        public double evaluate(String geom, String point) {
            return Functions.lineLocatePoint(
                    GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(point.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_LineMerge extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.lineMerge(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_LineStringFromText extends UDF {
        public String evaluate(String geomString, String delimiter) {
            return new String(GeometrySerde.serialize(Constructors.lineStringFromText(geomString, delimiter)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_LineSubstring extends UDF {
        public String evaluate(String geom, double fromFraction, double toFraction) {
            return new String(GeometrySerde.serialize(
                    Functions.lineSubString(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), fromFraction, toFraction)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_LinestringFromWKB extends UDF {
        public String evaluate(String wkb) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.lineFromWKB(wkb.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }

        public String evaluate(String wkb, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.lineFromWKB(wkb.getBytes(StandardCharsets.UTF_8), srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_LongestLine extends UDF {
        public String evaluate(String geom1, String geom2) {
            return new String(GeometrySerde.serialize(
                    Functions.longestLine(
                            GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)),
                            GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MLineFromText extends UDF {
        public String evaluate(String wkt) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.mLineFromText(wkt, 0)), StandardCharsets.UTF_8);
        }

        public String evaluate(String wkt, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.mLineFromText(wkt, srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MPointFromText extends UDF {
        public String evaluate(String wkt) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.mPointFromText(wkt, 0)), StandardCharsets.UTF_8);
        }

        public String evaluate(String wkt, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.mPointFromText(wkt, srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MPolyFromText extends UDF {
        public String evaluate(String wkt) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.mPolyFromText(wkt, 0)), StandardCharsets.UTF_8);
        }

        public String evaluate(String wkt, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.mPolyFromText(wkt, srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MakeEnvelope extends UDF {
        public String evaluate(double minX, double minY, double maxX, double maxY) {
            return new String(GeometrySerde.serialize(Constructors.makeEnvelope(minX, minY, maxX, maxY)), StandardCharsets.UTF_8);
        }

        public String evaluate(double minX, double minY, double maxX, double maxY, int srid) {
            return new String(GeometrySerde.serialize(Constructors.makeEnvelope(minX, minY, maxX, maxY, srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MakeLine extends UDF {
        public String evaluate(String geom1, String geom2) {
            return new String(GeometrySerde.serialize(Functions.makeLine(
                    GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.makeLine(GeometrySerde.deserialize2List(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MakePoint extends UDF {
        public String evaluate(double x, double y) {
            return new String(GeometrySerde.serialize(Constructors.makePoint(x, y, null, null)), StandardCharsets.UTF_8);
        }

        public String evaluate(double x, double y, double z) {
            return new String(GeometrySerde.serialize(Constructors.makePoint(x, y, z, null)), StandardCharsets.UTF_8);
        }

        public String evaluate(double x, double y, double z, double m) {
            return new String(GeometrySerde.serialize(Constructors.makePoint(x, y, z, m)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MakePolygon extends UDF {
        public String evaluate(String shell) {
            return new String(GeometrySerde.serialize(Functions.makePolygon(
                    GeometrySerde.deserialize(shell.getBytes(StandardCharsets.UTF_8)), null)), StandardCharsets.UTF_8);
        }

        public String evaluate(String shell, String holes) {
            return new String(GeometrySerde.serialize(Functions.makePolygon(
                    GeometrySerde.deserialize(shell.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize2List(holes.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MakeValid extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.makeValid(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), false)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, boolean keepCollapsed) {
            return new String(GeometrySerde.serialize(Functions.makeValid(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), keepCollapsed)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MaxDistance extends UDF {
        public double evaluate(String geom1, String geom2) {
            return Functions.maxDistance(
                    GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_MinimumBoundingCircle extends UDF {
        public String evaluate(String geometry, int quadrantSegments) {
            return new String(GeometrySerde.serialize(Functions.minimumBoundingCircle(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), quadrantSegments)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_MinimumClearance extends UDF {
        public double evaluate(String geometry) throws IOException {
            return Functions.minimumClearance(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_MinimumClearanceLine extends UDF {
        public String evaluate(String geometry) throws IOException {
            return new String(GeometrySerde.serialize(Functions.minimumClearanceLine(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Multi extends UDF {
        public String evaluate(String geometry) throws IOException {
            return new String(GeometrySerde.serialize(Functions.createMultiGeometryFromOneElement(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_NDims extends UDF {
        public int evaluate(String geometry) {
            return Functions.nDims(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_NPoints extends UDF {
        public int evaluate(String geometry) {
            return Functions.nPoints(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_NRings extends UDF {
        public int evaluate(String geom) throws Exception {
            return Functions.nRings(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Normalize extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.normalize(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_NumGeometries extends UDF {
        public int evaluate(String geometry) {
            return Functions.numGeometries(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_NumInteriorRing extends UDF {
        public Integer evaluate(String geometry) {
            return Functions.numInteriorRings(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_NumInteriorRings extends UDF {
        public Integer evaluate(String geometry) {
            return Functions.numInteriorRings(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_NumPoints extends UDF {
        public int evaluate(String geom) throws Exception {
            return Functions.numPoints(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_OrderingEquals extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.orderingEquals(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Overlaps extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.overlaps(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Perimeter extends UDF {
        public double evaluate(String geometry) {
            return Functions.perimeter(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }

        public double evaluate(String geometry, boolean use_spheroid) {
            return Functions.perimeter(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), use_spheroid);
        }

        public double evaluate(String geometry, boolean use_spheroid, boolean lenient) {
            return Functions.perimeter(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), use_spheroid, lenient);
        }
    }

    public static class ST_Perimeter2D extends UDF {
        public double evaluate(String geometry) {
            return Functions.perimeter(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }

        public double evaluate(String geometry, boolean use_spheroid) {
            return Functions.perimeter(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), use_spheroid);
        }

        public double evaluate(String geometry, boolean use_spheroid, boolean lenient) {
            return Functions.perimeter(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), use_spheroid, lenient);
        }
    }

    public static class ST_Point extends UDF {
        public String evaluate(double x, double y) {
            return new String(GeometrySerde.serialize(Constructors.point(x, y)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_PointFromGeoHash extends UDF {
        public String evaluate(String geoHash, Integer precision) {
            return new String(GeometrySerde.serialize(Constructors.pointFromGeoHash(geoHash, precision)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geoHash) {
            return new String(GeometrySerde.serialize(Constructors.pointFromGeoHash(geoHash, null)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_PointFromText extends UDF {
        public String evaluate(String geomString, String geomFormat) {
            return new String(GeometrySerde.serialize(Constructors.pointFromText(geomString, geomFormat)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_PointFromWKB extends UDF {
        public String evaluate(String wkb) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.pointFromWKB(wkb.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }

        public String evaluate(String wkb, int srid) throws ParseException {
            return new String(GeometrySerde.serialize(Constructors.pointFromWKB(wkb.getBytes(StandardCharsets.UTF_8), srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_PointN extends UDF {
        public String evaluate(String geometry, int n) {
            return new String(GeometrySerde.serialize(Functions.pointN(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), n)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_PointOnSurface extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.pointOnSurface(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_PointZ extends UDF {
        public String evaluate(double x, double y, double z) {
            return new String(GeometrySerde.serialize(Constructors.pointZ(x, y, z, 0)), StandardCharsets.UTF_8);
        }

        public String evaluate(double x, double y, double z, int srid) {
            return new String(GeometrySerde.serialize(Constructors.pointZ(x, y, z, srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Points extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.points(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Polygon extends UDF {
        public String evaluate(String geometry, int srid) {
            return new String(GeometrySerde.serialize(Functions.makepolygonWithSRID(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_PolygonFromEnvelope extends UDF {
        public String evaluate(double minX, double minY, double maxX, double maxY) {
            return new String(GeometrySerde.serialize(Constructors.polygonFromEnvelope(minX, minY, maxX, maxY)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_PolygonFromText extends UDF {
        public String evaluate(String geomString, String geomFormat) {
            return new String(GeometrySerde.serialize(Constructors.polygonFromText(geomString, geomFormat)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Polygonize extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.polygonize(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_PrecisionReduce extends UDF {
        public String evaluate(String geometry, int precisionScale) {
            return new String(GeometrySerde.serialize(Functions.reducePrecision(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), precisionScale)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Project extends UDF {
        public String evaluate(String point, double distance, double azimuth) {
            return new String(GeometrySerde.serialize(Functions.project(GeometrySerde.deserialize(point.getBytes(StandardCharsets.UTF_8)), distance, azimuth)), StandardCharsets.UTF_8);
        }

        public String evaluate(String point, double distance, double azimuth, boolean lenient) {
            return new String(GeometrySerde.serialize(Functions.project(GeometrySerde.deserialize(point.getBytes(StandardCharsets.UTF_8)), distance, azimuth, lenient)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ReducePrecision extends UDF {
        public String evaluate(String geometry, int precisionScale) {
            return new String(GeometrySerde.serialize(Functions.reducePrecision(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), precisionScale)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Relate extends UDF {
        public String evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.relate(GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)), GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }

        public Boolean evaluate(String geom1, String geom2, String intersectionMatrix) {
            return Predicates.relate(GeometrySerde.deserialize(geom1.getBytes(StandardCharsets.UTF_8)), GeometrySerde.deserialize(geom2.getBytes(StandardCharsets.UTF_8)), intersectionMatrix);
        }
    }

    public static class ST_RelateMatch extends UDF {
        public Boolean evaluate(String matrix1, String matrix2) {
            return Predicates.relateMatch(matrix1, matrix2);
        }
    }

    public static class ST_RemovePoint extends UDF {
        public String evaluate(String linestring) {
            return new String(GeometrySerde.serialize(Functions.removePoint(GeometrySerde.deserialize(linestring.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }

        public String evaluate(String linestring, int position) {
            return new String(GeometrySerde.serialize(Functions.removePoint(GeometrySerde.deserialize(linestring.getBytes(StandardCharsets.UTF_8)), position)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_RemoveRepeatedPoints extends UDF {
        public String evaluate(String geom) {
            return new String(GeometrySerde.serialize(Functions.removeRepeatedPoints(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, double tolerance) {
            return new String(GeometrySerde.serialize(Functions.removeRepeatedPoints(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), tolerance)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Reverse extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.reverse(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Rotate extends UDF {
        public String evaluate(String geom, double angle) {
            return new String(GeometrySerde.serialize(Functions.rotate(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), angle)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, double angle, String pointOrigin) {
            return new String(GeometrySerde.serialize(Functions.rotate(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), angle, GeometrySerde.deserialize(pointOrigin.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, double angle, double originX, double originY) {
            return new String(GeometrySerde.serialize(Functions.rotate(GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), angle, originX, originY)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_RotateX extends UDF {
        public String evaluate(String geometry, double angle) {
            return new String(GeometrySerde.serialize(Functions.rotateX(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), angle)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_RotateY extends UDF {
        public String evaluate(String geometry, double angle) {
            return new String(GeometrySerde.serialize(Functions.rotateY(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), angle)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_S2CellIDs extends UDF {
        public ArrayList<Long> evaluate(String input, int level) {
            return Arrays.stream(TypeUtils.castLong(Functions.s2CellIDs(GeometrySerde.deserialize(input.getBytes(StandardCharsets.UTF_8)), level))).boxed().collect(
                    Collectors.toCollection(ArrayList::new));
        }
    }

    public static class ST_SRID extends UDF {
        public int evaluate(String geometry) {
            return Functions.getSRID(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Scale extends UDF {
        public String evaluate(String geometry, double scaleX, double scaleY) {
            return new String(GeometrySerde.serialize(Functions.scale(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), scaleX, scaleY)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ScaleGeom extends UDF {
        public String evaluate(String geometry, String factor, String origin) {
            return new String(GeometrySerde.serialize(Functions.scaleGeom(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(factor.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(origin.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, String factor) {
            return new String(GeometrySerde.serialize(Functions.scaleGeom(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(factor.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_SetPoint extends UDF {
        public String evaluate(String linestring, int position, String point) {
            return new String(GeometrySerde.serialize(Functions.setPoint(
                    GeometrySerde.deserialize(linestring.getBytes(StandardCharsets.UTF_8)),
                    position,
                    GeometrySerde.deserialize(point.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_SetSRID extends UDF {
        public String evaluate(String geometry, int srid) {
            return new String(GeometrySerde.serialize(Functions.setSRID(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), srid)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_ShiftLongitude extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.shiftLongitude(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Simplify extends UDF {
        public String evaluate(String geometry, double distanceTolerance) {
            return new String(GeometrySerde.serialize(Functions.simplify(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), distanceTolerance)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_SimplifyPolygonHull extends UDF {
        public String evaluate(String geometry, double vertexFactor, boolean isOuter) {
            return new String(GeometrySerde.serialize(Functions.simplifyPolygonHull(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), vertexFactor, isOuter)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double vertexFactor) {
            return new String(GeometrySerde.serialize(Functions.simplifyPolygonHull(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), vertexFactor)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_SimplifyPreserveTopology extends UDF {
        public String evaluate(String geometry, double distanceTolerance) {
            return new String(GeometrySerde.serialize(Functions.simplifyPreserveTopology(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), distanceTolerance)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_SimplifyVW extends UDF {
        public String evaluate(String geometry, double distanceTolerance) {
            return new String(GeometrySerde.serialize(Functions.simplifyVW(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), distanceTolerance)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Snap extends UDF {
        public String evaluate(String input, String reference, double tolerance) {
            return new String(GeometrySerde.serialize(Functions.snap(
                    GeometrySerde.deserialize(input.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(reference.getBytes(StandardCharsets.UTF_8)),
                    tolerance)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Split extends UDF {
        public String evaluate(String input, String blade) {
            return new String(GeometrySerde.serialize(Functions.split(
                    GeometrySerde.deserialize(input.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(blade.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_StartPoint extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.startPoint(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_SubDivide extends UDF {
        public String evaluate(String geometry, int maxVertices) {
            return new String(GeometrySerde.serialize(Functions.subDivide(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), maxVertices)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_SymDifference extends UDF {
        public String evaluate(String leftGeom, String rightGeom) {
            return new String(GeometrySerde.serialize(Functions.symDifference(
                    GeometrySerde.deserialize(leftGeom.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Touches extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.touches(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Transform extends UDF {
        public String evaluate(String geometry, String sourceCRS, String targetCRS) {
            return new String(GeometrySerde.serialize(GeoToolsWrapper.transform(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)),
                    sourceCRS, targetCRS)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, String sourceCRS, String targetCRS, boolean lenient) {
            return new String(GeometrySerde.serialize(GeoToolsWrapper.transform(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)),
                    sourceCRS, targetCRS, lenient)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Translate extends UDF {
        public String evaluate(String geom, double deltaX, double deltaY) {
            return new String(GeometrySerde.serialize(Functions.translate(
                    GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), deltaX, deltaY)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geom, double deltaX, double deltaY, double deltaZ) {
            return new String(GeometrySerde.serialize(Functions.translate(
                    GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)), deltaX, deltaY, deltaZ)), StandardCharsets.UTF_8);
        }
    }

    public static class ST_TriangulatePolygon extends UDF {
        public String evaluate(String geom) {
            return new String(GeometrySerde.serialize(Functions.triangulatePolygon(
                    GeometrySerde.deserialize(geom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_UnaryUnion extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(Functions.unaryUnion(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Union extends UDF {
        public String evaluate(String leftGeom, String rightGeom) {
            return new String(GeometrySerde.serialize(Functions.union(
                    GeometrySerde.deserialize(leftGeom.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeom.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_VoronoiPolygons extends UDF {
        public String evaluate(String geometry) {
            return new String(GeometrySerde.serialize(FunctionsGeoTools.voronoiPolygons(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), 0.0, null)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double tolerance) {
            return new String(GeometrySerde.serialize(FunctionsGeoTools.voronoiPolygons(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)), tolerance, null)), StandardCharsets.UTF_8);
        }

        public String evaluate(String geometry, double tolerance, String extent) {
            return new String(GeometrySerde.serialize(FunctionsGeoTools.voronoiPolygons(
                    GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)),
                    tolerance,
                    GeometrySerde.deserialize(extent.getBytes(StandardCharsets.UTF_8)))), StandardCharsets.UTF_8);
        }
    }

    public static class ST_Within extends UDF {
        public boolean evaluate(String leftGeometry, String rightGeometry) {
            return Predicates.within(
                    GeometrySerde.deserialize(leftGeometry.getBytes(StandardCharsets.UTF_8)),
                    GeometrySerde.deserialize(rightGeometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_X extends UDF {
        public Double evaluate(String geometry) {
            return Functions.x(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_XMax extends UDF {
        public double evaluate(String geometry) {
            return Functions.xMax(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_XMin extends UDF {
        public double evaluate(String geometry) {
            return Functions.xMin(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Y extends UDF {
        public Double evaluate(String geometry) {
            return Functions.y(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_YMax extends UDF {
        public double evaluate(String geometry) {
            return Functions.yMax(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_YMin extends UDF {
        public double evaluate(String geometry) {
            return Functions.yMin(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_Z extends UDF {
        public Double evaluate(String geometry) {
            return Functions.z(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_ZMax extends UDF {
        public Double evaluate(String geometry) {
            return Functions.zMax(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }

    public static class ST_ZMin extends UDF {
        public Double evaluate(String geometry) {
            return Functions.zMin(GeometrySerde.deserialize(geometry.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
