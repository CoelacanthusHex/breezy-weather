package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.utils.DisplayUtils

// actual air quality = quality(μg/m³) * factor.
enum class AirQualityUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Float> {

    MUGPCUM("mugpcum", 1f);

    override val valueArrayId = R.array.air_quality_unit_values
    override val nameArrayId = R.array.air_quality_units
    override val voiceArrayId = R.array.air_quality_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Float) = valueInDefaultUnit * unitFactor

    override fun getValueInDefaultUnit(valueInCurrentUnit: Float) = valueInCurrentUnit / unitFactor

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Float
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 1)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueText(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueVoice(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )
}