package lance

import chisel3._
import chisel3.util._
import chisel3.core.withClock
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.config.{Field, Parameters}
import testchipip.GeneratorApp

case object BuildTop extends Field[(Clock, Bool, Parameters) => TinyLanceTopModuleImp[TinyLanceTop]]

class TinyLanceTestHarness(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val led = Output(Bool())
  })

  val slowClock = Module(new SlowClock)
  slowClock.io.clk_system := clock

  withClock (slowClock.io.clk_slow) {
    val dut = p(BuildTop)(clock, reset.toBool, p)
    dut.debug := DontCare
    dut.tieOffInterrupts()
    io.led := dut.led
  }
}

/** ボードのクロックでは動作しないので、クロックを下げるクラス。
  */
class SlowClock extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clk_system = Input(Clock())
    val clk_slow = Output(Clock())
  })
}

object Generator extends GeneratorApp {
  generateFirrtl
  generateAnno
}
