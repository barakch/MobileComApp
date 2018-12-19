package apps.shay.barak.mobilecomapp.adapter;

import apps.shay.barak.mobilecomapp.model.Series;

public class SeriesWithKey {

    private String key;
    private Series series;

    public SeriesWithKey(String key, Series series) {
        this.key = key;
        this.series = series;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }
}
