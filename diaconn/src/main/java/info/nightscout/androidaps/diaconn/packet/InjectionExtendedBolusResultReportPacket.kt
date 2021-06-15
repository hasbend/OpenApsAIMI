package info.nightscout.androidaps.diaconn.packet

import dagger.android.HasAndroidInjector
import info.nightscout.androidaps.diaconn.DiaconnG8Pump
import info.nightscout.androidaps.logging.LTag
import info.nightscout.androidaps.plugins.bus.RxBusWrapper
import info.nightscout.androidaps.utils.resources.ResourceHelper
import javax.inject.Inject

/**
 * InjectionExtendedBolusResultReportPacket
 */
class InjectionExtendedBolusResultReportPacket(injector: HasAndroidInjector) : DiaconnG8Packet(injector ) {

    @Inject lateinit var diaconnG8Pump: DiaconnG8Pump
    @Inject lateinit var rxBus: RxBusWrapper
    @Inject lateinit var resourceHelper: ResourceHelper

    init {
        msgType = 0xe5.toByte()
        aapsLogger.debug(LTag.PUMPCOMM, "InjectionExtendedBolusResultReportPacket init ")
    }

    override fun handleMessage(data: ByteArray?) {
        val defectCheck = defect(data)
        if (defectCheck != 0) {
            aapsLogger.debug(LTag.PUMPCOMM, "InjectionExtendedBolusResultReportPacket Got some Error")
            failed = true
            return
        } else failed = false

        val bufferData = prefixDecode(data)

        val result = getByteToInt(bufferData) // 0: success , 1: user stop, 2:fail
        val settingMinutes = getShortToInt(bufferData)
        val elapsedTime = getShortToInt(bufferData)
        val bolusAmountToBeDelivered  = getShortToInt(bufferData) / 100.0
        val deliveredBolusAmount = getShortToInt(bufferData) / 100.0

        //diaconnG8Pump.isExtendedInProgress = result == 0
        diaconnG8Pump.extendedBolusMinutes = settingMinutes
        diaconnG8Pump.extendedBolusAbsoluteRate = bolusAmountToBeDelivered
        diaconnG8Pump.extendedBolusPassedMinutes = elapsedTime
        diaconnG8Pump.extendedBolusRemainingMinutes = settingMinutes - elapsedTime
        diaconnG8Pump.extendedBolusDeliveredSoFar = deliveredBolusAmount

        aapsLogger.debug(LTag.PUMPCOMM, "Result: $result")
        aapsLogger.debug(LTag.PUMPCOMM, "Extended bolus running: " + diaconnG8Pump.extendedBolusAbsoluteRate + " U/h")
        aapsLogger.debug(LTag.PUMPCOMM, "Extended bolus duration: " + diaconnG8Pump.extendedBolusMinutes + " min")
        aapsLogger.debug(LTag.PUMPCOMM, "Extended bolus so far: " + diaconnG8Pump.extendedBolusSoFarInMinutes + " min")
        aapsLogger.debug(LTag.PUMPCOMM, "Extended bolus remaining minutes: " + diaconnG8Pump.extendedBolusRemainingMinutes + " min")
        aapsLogger.debug(LTag.PUMPCOMM, "Extended bolus delivered so far: " + diaconnG8Pump.extendedBolusDeliveredSoFar + " U")
    }

    override fun getFriendlyName(): String {
        return "PUMP_INJECTION_EXTENDED_BOLUS_RESULT_REPORT"
    }
}