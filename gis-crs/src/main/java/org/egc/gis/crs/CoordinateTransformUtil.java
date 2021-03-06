package org.egc.gis.crs;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.awt.geom.Rectangle2D;

/**
 * Description:
 * <pre>
 * https://www.programcreek.com/java-api-examples/?class=org.geotools.geometry.jts.JTS&method=transform
 * </pre>
 *
 * @author houzhiwei
 * @date 2018 /10/7 15:08
 */
@Slf4j
public class CoordinateTransformUtil {
    private CoordinateTransformUtil() {}

    /**
     * Transform with epsg coordinate.
     *
     * @param fromEPSG 源 epsg, 如 4326
     * @param toEPSG   目标 epsg
     * @param x        x 坐标或<b>纬度/lat</b>
     * @param y        y 坐标或<b>经度/lon</b>
     * @return the coordinate
     * @throws TransformException the transform exception
     * @throws FactoryException   the factory exception
     */
    public static Coordinate transformEPSGCoordinate(int fromEPSG, int toEPSG, double x, double y) throws TransformException, FactoryException {
        return transformCoordinate("EPSG:" + fromEPSG, "EPSG:" + toEPSG, x, y);
    }

    /**
     * Transform coordinate.
     *
     * @param fromAuthority the source AuthorityCode, e.g. "EPSG:4326", "AUTO:42001"
     * @param toAuthority   the target AuthorityCode
     * @param x             x 坐标或<b>纬度/lat</b>
     * @param y             y 坐标或<b>经度/lon</b>
     * @return the coordinate
     * @throws TransformException the transform exception
     * @throws FactoryException   the factory exception
     */
    public static Coordinate transformCoordinate(String fromAuthority, String toAuthority, double x, double y) throws TransformException, FactoryException {
        CoordinateReferenceSystem sourceCRS = CRS.decode(fromAuthority);
        CoordinateReferenceSystem targetCRS = CRS.decode(toAuthority);
        // allow for some error due to different datums
        boolean lenient = true;
        MathTransform mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, lenient);
        // x 竖轴，纬度
        // y 横轴，经度
        Coordinate source = new Coordinate(x, y);
        Coordinate target = new Coordinate();
        JTS.transform(source, target, mathTransform);
        return target;
    }


    /**
     * Transform envelope referenced envelope.
     *
     * @param fromAuthority the from authority
     * @param toAuthority   the to authority
     * @param bounds        the bounds
     * @return the referenced envelope
     * @throws FactoryException the factory exception
     */
    public static ReferencedEnvelope transformEnvelope(String fromAuthority, String toAuthority, Rectangle2D bounds) throws FactoryException {
        CoordinateReferenceSystem fromCRS = CRS.decode(fromAuthority);
        CoordinateReferenceSystem toCRS = CRS.decode(toAuthority);
        try {
            MathTransform transform = CRS.findMathTransform(fromCRS, toCRS);
            Envelope sourceEnvelope = new Envelope(bounds.getMinX(), bounds.getMaxX(),
                                                   bounds.getMinY(), bounds.getMaxY());
            final Envelope envelope = JTS.transform(sourceEnvelope, transform);
            return new ReferencedEnvelope(envelope.getMinX(), envelope.getMaxX(),
                                          envelope.getMinY(), envelope.getMaxY(),
                                          toCRS);
        } catch (FactoryException | TransformException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * From beijing 54 to wgs 84 coordinate.
     * https://blog.csdn.net/breaker892902/article/details/9069609
     *
     * @param lon 经度
     * @param lat 纬度
     * @return the coordinate <ul><li>{@link Coordinate#y} 经度</li><li>{@link Coordinate#x} 纬度</li></ul>
     * @throws TransformException the transform exception
     * @throws FactoryException   the factory exception
     */
    public static Coordinate fromBeijing54ToWGS84(double lon, double lat) throws TransformException, FactoryException {
        return transformEPSGCoordinate(4214, 4326, lat, lon);
    }
}
