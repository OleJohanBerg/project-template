-- Copyright 1986-2018 Xilinx, Inc. All Rights Reserved.
-- --------------------------------------------------------------------------------
-- Tool Version: Vivado v.2018.1 (lin64) Build 2188600 Wed Apr  4 18:39:19 MDT 2018
-- Date        : Sun Jun 24 18:11:27 2018
-- Host        : palladium running 64-bit Ubuntu 18.04 LTS
-- Command     : write_vhdl -force -mode synth_stub
--               /home/tetsuya/fpga/rocket-chip-template/vprj/TinyLance.srcs/sources_1/ip/SlowClock/SlowClock_stub.vhdl
-- Design      : SlowClock
-- Purpose     : Stub declaration of top-level module interface
-- Device      : xc7a100tcsg324-1
-- --------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity SlowClock is
  Port ( 
    clk_slow : out STD_LOGIC;
    clk_system : in STD_LOGIC
  );

end SlowClock;

architecture stub of SlowClock is
attribute syn_black_box : boolean;
attribute black_box_pad_pin : string;
attribute syn_black_box of stub : architecture is true;
attribute black_box_pad_pin of stub : architecture is "clk_slow,clk_system";
begin
end;
