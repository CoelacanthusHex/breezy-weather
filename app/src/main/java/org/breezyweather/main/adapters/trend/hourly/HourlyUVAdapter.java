package org.breezyweather.main.adapters.trend.hourly;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import org.breezyweather.common.basic.GeoActivity;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.weather.Hourly;
import org.breezyweather.common.basic.models.weather.UV;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.ui.widgets.trend.TrendRecyclerView;
import org.breezyweather.common.ui.widgets.trend.chart.PolylineAndHistogramView;
import org.breezyweather.theme.ThemeManager;
import org.breezyweather.theme.weatherView.WeatherViewController;
import org.breezyweather.R;
import org.breezyweather.main.utils.MainThemeColorProvider;

/**
 * Hourly UV adapter.
 * */

public class HourlyUVAdapter extends AbsHourlyTrendAdapter {

    private int mHighestIndex;

    class ViewHolder extends AbsHourlyTrendAdapter.ViewHolder {

        private final PolylineAndHistogramView mPolylineAndHistogramView;

        ViewHolder(View itemView) {
            super(itemView);
            mPolylineAndHistogramView = new PolylineAndHistogramView(itemView.getContext());
            hourlyItem.setChartItemView(mPolylineAndHistogramView);
        }

        @SuppressLint({"SetTextI18n, InflateParams", "DefaultLocale"})
        void onBindView(GeoActivity activity, Location location, int position) {
            StringBuilder talkBackBuilder = new StringBuilder(activity.getString(R.string.tag_uv));

            super.onBindView(activity, location, talkBackBuilder, position);

            Weather weather = location.getWeather();
            assert weather != null;
            Hourly hourly = weather.getHourlyForecast().get(position);

            if (hourly.getUV() != null) {
                Integer index = hourly.getUV().getIndex();
                talkBackBuilder.append(", ").append(index).append(", ").append(hourly.getUV().getLevel());
                mPolylineAndHistogramView.setData(
                        null, null,
                        null, null,
                        null, null,
                        (float) (index == null ? 0 : index),
                        String.format("%d", index == null ? 0 : index),
                        (float) mHighestIndex,
                        0f
                );
                mPolylineAndHistogramView.setLineColors(
                        hourly.getUV().getUVColor(activity),
                        hourly.getUV().getUVColor(activity),
                        MainThemeColorProvider.getColor(location, com.google.android.material.R.attr.colorOutline)
                );
            }
            int[] themeColors = ThemeManager
                    .getInstance(itemView.getContext())
                    .getWeatherThemeDelegate()
                    .getThemeColors(
                            itemView.getContext(),
                            WeatherViewController.getWeatherKind(location.getWeather()),
                            location.isDaylight()
                    );
            boolean lightTheme = MainThemeColorProvider.isLightTheme(itemView.getContext(), location);
            mPolylineAndHistogramView.setShadowColors(
                    themeColors[lightTheme ? 1 : 2],
                    themeColors[2],
                    lightTheme
            );
            mPolylineAndHistogramView.setTextColors(
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText),
                    MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
            );
            mPolylineAndHistogramView.setHistogramAlpha(lightTheme ? 1f : 0.5f);

            hourlyItem.setContentDescription(talkBackBuilder.toString());
        }
    }

    public HourlyUVAdapter(GeoActivity activity, Location location) {
        super(activity, location);

        Weather weather = location.getWeather();
        assert weather != null;

        mHighestIndex = 0;
        for (int i = weather.getHourlyForecast().size() - 1; i >= 0; i --) {
            if (weather.getHourlyForecast().get(i).getUV() != null) {
                Integer index = weather.getHourlyForecast().get(i).getUV().getIndex();
                if (index != null && index > mHighestIndex) {
                    mHighestIndex = index;
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend_hourly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsHourlyTrendAdapter.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBindView(getActivity(), getLocation(), position);
    }

    @Override
    public int getItemCount() {
        return getLocation().getWeather().getHourlyForecast().size();
    }

    @Override
    public boolean isValid(Location location) {
        return mHighestIndex > 0;
    }

    @Override
    public String getDisplayName(Context context) {
        return context.getString(R.string.tag_uv);
    }

    @Override
    public void bindBackgroundForHost(TrendRecyclerView host) {
        List<TrendRecyclerView.KeyLine> keyLineList = new ArrayList<>();
        keyLineList.add(
                new TrendRecyclerView.KeyLine(
                        UV.UV_INDEX_HIGH,
                        String.valueOf(UV.UV_INDEX_HIGH),
                        getActivity().getString(R.string.action_alert),
                        TrendRecyclerView.KeyLine.ContentPosition.ABOVE_LINE
                )
        );
        host.setData(keyLineList, mHighestIndex, 0);
    }
}