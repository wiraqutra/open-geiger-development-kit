package org.fukushima.OpenGeiger.AutoClient;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;

public class PinOverlay extends ItemizedOverlay<PinOverlayItem> {

    private List<GeoPoint> points = new ArrayList<GeoPoint>();

    public PinOverlay(Drawable defaultMarker) {
        super( boundCenterBottom(defaultMarker) );
    }

    @Override
    protected PinOverlayItem createItem(int i) {
        GeoPoint point = points.get(i);
        return new PinOverlayItem(point);
    }

    @Override
    public int size() {
        return points.size();
    }

    public void addPoint(GeoPoint point) {
        this.points.add(point);
        populate();
    }
	
    public void clearPoint() {
        this.points.clear();
        populate();
    }
}