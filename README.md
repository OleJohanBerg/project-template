# RISC-V Projectのテンプレートの和訳や解説をするフォーク

ここでは、[フォーク元](https://github.com/ucb-bar/project-template) の和訳や解説をします。

# RISC-V Projectのテンプレート

本リポジトリは、RISC-Vをカスタマイズするプロジェクト用の、初心者向けテンプレートです。
これは、Chisel HDLとRocketChip SoC generatorを利用して、メモリ・マップドIO周辺機器、DMAや独自アクセラレータを追加したRISC-V SoCを生成するのに役立ちます。

## はじめに

### ソースのチェックアウト

本リポジトリをクローンして、全てのサブモジュールを初期化する必要があります。

    git clone https://github.com/horie-t/project-template.git
    cd project-template
    git submodule update --init --recursive

### ツールのビルド

toolsリポジトリに含まれるものは、クロス・コンパイラ、フロントエンド・サーバ、プロキシ・カーネルといった、コードをRISC-Vの命令にコンパイルし、設計したSoC上で実行するのに必要なものが含まれます。詳細は、 https://github.com/riscv/riscv-tools にあります。
基本的なインストールをする場合は、単に以下のステップが必要なだけです。

    # シェルのプロファイルに、以下の2行を追加してもよい。
    export RISCV=/path/to/install/dir
    export PATH=$RISCV/bin:$PATH

    cd rocket-chip/riscv-tools
    ./build.sh

### コンパイルと、Verilatorのシミュレーションの実行

実施例の設計をコンパイルするには、"verisim" ディレクトリで make を実行します。
これによって、実施例のプロジェクトの DefaultExampleConfig が、エラボレートされます。

simulator-example-DefaultExampleConfig という名前の実行ファイルが生成されます。
この実行ファイルを使って、どんなRV64互換のコードを実行する事も可能です。例えば、 
以下のようにriscv-tools のアセンブリテストの一つを実行できます。

    ./simulator-example-DefaultExampleConfig $RISCV/riscv64-unknown-elf/share/riscv-tests/isa/rv64ui-p-simple

もし、後ほど、あなた独自のプロジェクトを作成した場合、環境変数を使って、別の構築設定でビルドできます。

    make PROJECT=yourproject CONFIG=YourConfig
    ./simulator-yourproject-YourConfig ...

## サブモジュールとサブディレクトリ

このプロジェクト・テンプレートのサブモジュールとサブディレクトリの構成は以下の通りです。

 * rocket-chip - RocketChipジェネレータとChisel HDLのコードを含みます。
 * testchipip - シリアル・アダプタ、ブロック・デバイスと、関連するVerilogとC++のコードを含みます。
 * testchipip/bootrom - Boot ROMに含まれる、第一段階ブート・ローダーのソースコード
 * verisim - Verilatorシミュレーションのコンパイルと実行用のディレクトリ
 * vsim - Synopsys VCSシミュレーションのコンパイルと実行用のディレクトリ
 * src/main/scala - あなた独自のScalaソースコードをここに追加します。

## ブロック・デバイスの使用

デフォルトの実施例のプロジェクトは、Rocketコア構造体(coreplex)、メモリとシリアル・ラインのみを
提供します。ですが、testchipipでは、他に、不揮発性ストレージに使用できるのブロック・デバイスの
シミュレーションを提供しています。blkdevパッケージを使用して、ブロック・デバイスを含むシミュレータを
ビルドできます。

    make CONFIG=SimBlockDeviceConfig
    ./simulator-example-SimBlockDeviceConfig +blkdev=block-device.img ...

+blkdev引数をシミュレータのコマンドラインに渡すと、RTLシミュレーションで、ファイルから
読み書きを実行できます。tests/blkdev.c を見れば、Rocketでブロック・デバイス用コントローラを
プログラムできるのかの例を見れます。

## MMIO周辺機器の追加

RocketChip向けに、独自のメモリ・マップド・IO機器を作成して、あなた独自のSocの設計に追加する事ができます。
最も簡単な作成方法は、TileLink向けの周辺機器を作成し、 TLRegisterRouter を使う方法です。 TLRegisterRouter は 
TileLink プロトコルを扱うための詳細を抽象化し、メモリ・マップされたレジスタを作成するのに簡単なインターフェイスを提供します。 
RegisterRouterベースの周辺機器を作成するには、まずコンフィグレーションの設定パラメータのcaseクラスを作成します。
次にトップレベル・モジュールに追加するポートのtraitバンドルクラスを作成します。最後に実際のRTLを含むモジュールの実装を作成します。

```scala
    case class PWMParams(address: BigInt, beatBytes: Int)

    trait PWMTLBundle extends Bundle {
      val pwmout = Output(Bool())
    }

    trait PWMTLModule {
      val io: PWMTLBundle
      implicit val p: Parameters
      def params: PWMParams

      val w = params.beatBytes * 8
      val period = Reg(UInt(w.W))
      val duty = Reg(UInt(w.W))
      val enable = RegInit(false.B)

      // レジスタの値を使って io.pwmout を制御する...

      regmap(
        0x00 -> Seq(
          RegField(w, period)),
        0x04 -> Seq(
          RegField(w, duty)),
        0x08 -> Seq(
          RegField(1, enable)))
    }
```

これらのクラスが出来たら、TLRegisterRouter を継承した周辺機器クラスを構築できます。
TLRegisterRouterのコンストラクタには適切な引数を渡します。
最初の引数のセットは、RegisterRouterがグローバル・アドレスマップの中のどこに配置すればよいかと、デバイス・ツリーのエントリに追加する時の情報です。二番目の引数は、IOバンドルのコンストラクタ(追加したいバンドルtraitでTLRegBundleを拡張します)です。最後の引数は、モジュールのコンストラクタ(追加したいモジュールtraitでTLRegModuleを拡張します)です。

```scala
    class PWMTL(c: PWMParams)(implicit p: Parameters)
      extends TLRegisterRouter(
        c.address, "pwm", Seq("ucbbar,pwm"),
        beatBytes = c.beatBytes)(
          new TLRegBundle(c, _) with PWMTLBundle)(
          new TLRegModule(c, _, _) with PWMTLModule)
```

コメント付きの完全なモジュールのコードは src/main/scala/example/PWM.scala にあります。

モジュールを作成したら、SoCに接続する必要があります。Rocketchipでは、接続に [cake pattern](http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth) を使います。
cake patternでは基本的に、traitの中にコード埋め込む事が必要になります。
RocketChipのcake patternでは、2種類のtraitがあります: LazyModule traitと、モジュールの実装のtraitです。

LazyModule traitは、全てのハードウェアがエラボレートされる前に実行されなければならない
セット・アップのコードを実行します。単純なメモリ・マップド周辺機器に対しては、
周辺機器のTileLink向けノードとMMIOクロスバーを接続するだけです。

```scala
    trait HasPeripheryPWM extends HasSystemNetworks {
      implicit val p: Parameters

      private val address = 0x2000

      val pwm = LazyModule(new PWMTL(
        PWMParams(address, peripheryBusConfig.beatBytes))(p))

      pwm.node := TLFragmenter(
        peripheryBusConfig.beatBytes, cacheBlockBytes)(peripheryBus.node)
    }
```

注意すべき点は、register routerのクラスから作成したPWMTLクラスは、
それ自身では、LazyModuleである点です。Register routerは、単に "node" 
と名付けられたTileLinke nodeを持っていて、nodeは、RocketChipの周辺機器用バス
(peripheryBus)に接続できます。これによって、周辺機器のためのアドレスマップと
デバイスツリーのエントリが、自動的に追加されます。

モジュールを実装したtraitは、独自のPWMモジュールをインスタンス化する場所であり、
SoCの残りの部分に接続します。接続用に、このモジュールは、`pwmout`という特別なOutputを持ち、
このtraitの中でそれを宣言します。宣言には、Chiselの マルチIO機能を使用します。
次に、PWMTLのpwmoutと、ここで宣言したpwmoutを接続します。

```scala
    trait HasPeripheryPWMModuleImp extends LazyMultiIOModuleImp {
      implicit val p: Parameters
      val outer: HasPeripheryPWM

      val pwmout = IO(Output(Bool()))

      pwmout := outer.pwm.module.io.pwmout
    }
```

では、次にこのtraitを、全体のシステムの中に入れましょう。以下のコードは、
src/main/scala/example/Top.scala から取ったものです。

```scala
    class ExampleTopWithPWM(q: Parameters) extends ExampleTop(q)
        with PeripheryPWM {
      override lazy val module = Module(
        new ExampleTopWithPWMModule(p, this))
    }

    class ExampleTopWithPWMModule(l: ExampleTopWithPWM)
      extends ExampleTopModule(l) with HasPeripheryPWMModuleImp
```

必要なのは、単純に LazyModule と モジュールの実装を分離する事です。システムをビルドするには、
2つのクラスが必要です。ExampleTop関連のクラスは、すでに必要な周辺機器を含んでいるので、これらを
単に継承するだけです。

ExampleTop関連のクラスに含まれるのは、エラボレーション前のコードと、
(LazyModuleによる)モジュールの実装のためのlazy変数です。
ExampleTopModuleクラスが、合成される実際のRTLです。

最後に、src/main/scala/example/Configs.scala にある構成設定用クラスに追加し、 
TestHarness がデフォルトの ExampleTop の代わりに ExampleTopWithPWM 
をインスタンス化するように指示します。

```scala
    class WithPWM extends Config((site, here, up) => {
      case BuildTop => (p: Parameters) =>
        Module(LazyModule(new ExampleTopWithPWM()(p)).module)
    })

    class PWMConfig extends Config(new WithPWM ++ new BaseExampleConfig)
```

ここまでで、PWMが動作するかテスト出来るようになりました。テストプログラムは tests/pwm.c にあります。

```c
    #define PWM_PERIOD 0x2000
    #define PWM_DUTY 0x2008
    #define PWM_ENABLE 0x2010

    static inline void write_reg(unsigned long addr, unsigned long data)
    {
            volatile unsigned long *ptr = (volatile unsigned long *) addr;
            *ptr = data;
    }

    static inline unsigned long read_reg(unsigned long addr)
    {
            volatile unsigned long *ptr = (volatile unsigned long *) addr;
            return *ptr;
    }

    int main(void)
    {
            write_reg(PWM_PERIOD, 20);
            write_reg(PWM_DUTY, 5);
            write_reg(PWM_ENABLE, 1);
    }
```

これは、前述で定義したレジスタに値を書き出しているだけです。モジュールのMMIOのベースアドレスは、0x2000です。
この値は、Verilogコードを生成する時に、アドレス・マップの部分で表示されます。

このプログラムを、`pwm.riscv`の実行ファイルとなるようにコンパイルします。

これで、全てが完了しました。では、シミュレーションを実行しましょう。

    cd verisim
    make CONFIG=PWMConfig
    ./simulator-example-PWMConfig ../tests/pwm.riscv

## DMAポートの追加

上記の例の中で、プロセッサが、MMIOを通して周辺機器との通信を許可しました。しかし、
IO機器(ディスクやネットワークドライバのようなもの)には、デバイスに対して、
メモリの一貫性システム(coherent memory system)を通さずに直接書き込みをしたくなります。
このようなデバイスを追加するには、以下のようにします。

```scala
    class DMADevice(implicit p: Parameters) extends LazyModule {
      val node = TLClientNode(TLClientParameters(
        name = "dma-device", sourceId = IdRange(0, 1)))

      lazy val module = new DMADeviceModule(this)
    }

    class DMADeviceModule(outer: DMADevice) extends LazyModuleImp(outer) {
      val io = IO(new Bundle {
        val mem = outer.node.bundleOut
        val ext = new ExtBundle
      })

      // ... rest of the code ...
    }

    trait HasPeripheryDMA extends HasSystemNetworks {
      implicit val p: Parameters

      val dma = LazyModule(new DMADevice)

      fsb.node := dma.node
    }

    trait HasPeripheryDMAModuleImp extends LazyMultiIOModuleImp {
      val ext = IO(new ExtBundle)
      ext <> outer.dma.module.io.ext
    }
```

`ExtBundle` は、チップ外のデータを取得したいデバイスと接続する信号線を含みます。
また、DMAデバイスは、TileLinkのクライアント用のポートを持ち、そのポートは、
front-side buffer (fsb)を通して、L1-L2 クロスバーに接続されます。
TLClientNodeのインスタンス化で与えられるsouceId変数は、このデバイスからの要求メッセージで
使われるidの範囲を決定します。ここでは、[0, 1)を範囲として指定したので、ID 0だけが使われます。

## RoCCアクセラレータの追加

周辺機器に加えて、RocketChipベースのSoCは、コプロセッサ型のアクセラレータで
カスタマイズできます。各コアで4つのアクセラレータを持つ事ができ、それぞれが
カスタムの命令で制御され、CPUとリソースを共有します。

### Rocc命令

コプロセッサへの命令は以下の書式です。

    customX rd, rs1, rs2, funct

ここで、Xは、0-3 の番号で、どのアクセラレータに命令を送るのかを制御するオペコードです。
`rd`、 `rs1` と `rs2` フィールドは、宛先のレジスタと、2つの元のレジスタの番号です。
`funct` は7ビットの整数で、アクセラレータが、命令の種別を判別するために使われます。

### アクセラレータの作成

RoCCアクセラレータはLazyモジュールであり、 LazyRoCCクラスを継承します。
これらの実装は、LazyRoCCModuleを継承すべきです。

```scala
    class CustomAccelerator(implicit p: Parameters) extends LazyRoCC {
      override lazy val module = new CustomAcceleratorModule(this)
    }

    class CustomAcceleratorModule(outer: CustomAccelerator) extends LazyRoCCModule(outer) {
      val cmd = Queue(io.cmd)
      // The parts of the command are as follows
      // inst - the parts of the instruction itself
      //   opcode
      //   rd - destination register number
      //   rs1 - first source register number
      //   rs2 - second source register number
      //   funct
      //   xd - is the destination register being used?
      //   xs1 - is the first source register being used?
      //   xs2 - is the second source register being used?
      // rs1 - the value of source register 1
      // rs2 - the value of source register 2
      ...
    }
```

LazyRoCC クラスは、2つの TLOutputNode のインスタンスを含み、 それらは、
`atlNode` と `tlNode` となっています。
前者は、L1命令キャッシュのバックサイドと協調して、tile-local arbiterと接続されます。
後者は、L1-L2 クロスバーに直接接続されます。
モジュールの実装のIOバンドルの中の、対応するTileLinkのポートは、それぞれ、`atl` と `tl` です。

アクセラレータが利用できるその他のインターフェイスは次の通りです。 `mem` は、
L1キャッシュへのアクセスを提供します; `ptw` は、page-table walker へのアクセスを
提供します; `busy` シグナルは、アクセラレータがまだ命令を取り扱っている状態を示します;
`interrupt` は、CPUに割り込みするのに使われます。

他のIOに関する詳細情報は、rocket-chip/src/main/scala/tile/LazyRocc.scala を見て下さい。

### ConfigへのRoCCアクセラレータの追加

RoCCアクセラレータをコアに追加するには、構築設定のBuildRoCCパラメータをオーバーライドします。
これは、RoccParameterのオブジェクトのシーケンスを受け取ります。このパラメータオブジェクトは
追加する各アクセラレータに付き一つです。また、このオブジェクトには2つのフィールドが要求されており、
それは、どのアクセラレータに転送するかを決定する `opcodes` と、どのようにアクセラレータを
ビルドするのかを指定する `generator` のフィールドです。
例えば、 前述で定義したアクセラレータを追加し、custom0 と custom1 命令を転送したいなら、
以下のようにできます。

```scala
    class WithCustomAccelerator extends Config((site, here, up) => {
      case RocketTilesKey => up(RocketTilesKey, site).map { r =>
        r.copy(rocc = Seq(
          RoCCParams(
            opcodes = OpcodeSet.custom0 | OpcodeSet.custom1,
            generator = (p: Parameters) => LazyModule(new CustomAccelerator()(p)))))
      }
    })

    class CustomAcceleratorConfig extends Config(
      new WithCustomAccelerator ++ new BaseConfig)
```

## サブモジュールの追加

開発の間に、Chiselのコードをサブモジュールの中にまとめて、異なるプロジェクトで共有
したくなるかもしれません。サブモジュールを、このプロジェクト・テンプレートに追加する
には、サブモジュールのプロジェクトを次のように構成します。

    yourproject/
        build.sbt
        src/main/scala/
            YourFile.scala

これをgitリポジトリに加えて、外部からアクセス可能な状態にします。そして、gitリポジトリを
このプロジェクトテンプレートに、サブモジュールとして追加します。

    git submodule add https://git-repository.com/yourproject.git

そして次に、Makefragファイルの `EXTRA_PACKAGES` 変数に `yourproject` を追加します。
これで、yourproject は、rocket-chipとtestchipipライブラリと一緒にjarファイルに一纏めに
されます。これで、サブモジュールで定義されたクラスを新しいプロジェクトで、importできます。
