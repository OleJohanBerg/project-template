// Copyright 1986-2018 Xilinx, Inc. All Rights Reserved.
// --------------------------------------------------------------------------------
// Tool Version: Vivado v.2018.1 (lin64) Build 2188600 Wed Apr  4 18:39:19 MDT 2018
// Date        : Sun Jun 24 18:11:26 2018
// Host        : palladium running 64-bit Ubuntu 18.04 LTS
// Command     : write_verilog -force -mode synth_stub
//               /home/tetsuya/fpga/rocket-chip-template/vprj/TinyLance.srcs/sources_1/ip/SlowClock/SlowClock_stub.v
// Design      : SlowClock
// Purpose     : Stub declaration of top-level module interface
// Device      : xc7a100tcsg324-1
// --------------------------------------------------------------------------------

// This empty module with port declaration file causes synthesis tools to infer a black box for IP.
// The synthesis directives are for Synopsys Synplify support to prevent IO buffer insertion.
// Please paste the declaration into a Verilog source file or add the file as an additional source.
module SlowClock(clk_slow, clk_system)
/* synthesis syn_black_box black_box_pad_pin="clk_slow,clk_system" */;
  output clk_slow;
  input clk_system;
endmodule
