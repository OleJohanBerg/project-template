package lance

import chisel3._
import chisel3.util._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.config.{Parameters, Field}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper.{HasRegMap, RegField}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.UIntIsOneOf

case class LEDParams(
  address: BigInt = 0x10012000,
  beatBytes: Int = 4)
case object LEDParams extends Field[LEDParams]

/** LED点灯制御モジュール
  */
class LEDBase extends Module {
  val io = IO(new Bundle {
    val light = Input(Bool())
    val led = Output(Bool())
  })

  io.led := io.light
}

/** LED点灯用TileLinkバンドル
  */
trait LEDTLBundle extends Bundle {
  val led = Output(Bool())
}

trait LEDTLModule extends HasRegMap {
  val io: LEDTLBundle
  implicit val p: Parameters
  def params: LEDParams

  val light = RegInit(false.B)

  val base = Module(new LEDBase)
  io.led := base.io.led
  base.io.light := light

  regmap(
    0x00 -> Seq(
      RegField(1, light)))
}

class LEDTL(c: LEDParams)(implicit p: Parameters)
  extends TLRegisterRouter(
    c.address, "led", Seq("lance,led"),
    beatBytes = c.beatBytes)(
      new TLRegBundle(c, _) with LEDTLBundle)(
      new TLRegModule(c, _, _) with LEDTLModule)

trait HasPeripheryLED { this: BaseSubsystem =>
  private val params = p(LEDParams)

  private val portName = "led"

  val led = LazyModule(new LEDTL(
    LEDParams(params.address, pbus.beatBytes))(p))

  pbus.toVariableWidthSlave(Some(portName)) { led.node }
}

trait HasPeripheryLEDModuleImp extends LazyModuleImp {
  implicit val p: Parameters
  val outer: HasPeripheryLED

  val led = IO(Output(Bool()))

  led := outer.led.module.io.led
}
