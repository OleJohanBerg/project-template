package lance

import chisel3._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.util.DontTouch
import testchipip._

class TinyLanceTop(implicit p: Parameters) extends RocketSubsystem
    with HasPeripheryLED
    with HasPeripheryBootROM
    with HasSyncExtInterrupts {
  override lazy val module = new TinyLanceTopModuleImp(this)
}

class TinyLanceTopModuleImp[+L <: TinyLanceTop](l: L) extends RocketSubsystemModuleImp(l)
    with HasPeripheryLEDModuleImp
    with HasPeripheryBootROMModuleImp
    with HasExtInterruptsModuleImp
