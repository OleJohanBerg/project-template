package lance

import chisel3._
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.config.{Field, Parameters}
import testchipip.GeneratorApp

case object BuildTop extends Field[(Clock, Bool, Parameters) => TinyLanceTopModuleImp[TinyLanceTop]]

class TinyLanceTestHarness(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val led = Output(Bool())
  })

  val dut = p(BuildTop)(clock, reset.toBool, p)
  dut.debug := DontCare
  dut.tieOffInterrupts()
  io.led := true.B
}

object Generator extends GeneratorApp {
  generateFirrtl
  generateAnno
}
