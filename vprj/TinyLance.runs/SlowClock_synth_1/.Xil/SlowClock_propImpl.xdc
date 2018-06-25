set_property SRC_FILE_INFO {cfile:/home/tetsuya/fpga/rocket-chip-template/vprj/TinyLance.srcs/sources_1/ip/SlowClock/SlowClock.xdc rfile:../../../TinyLance.srcs/sources_1/ip/SlowClock/SlowClock.xdc id:1 order:EARLY scoped_inst:inst} [current_design]
set_property src_info {type:SCOPED_XDC file:1 line:57 export:INPUT save:INPUT read:READ} [current_design]
set_input_jitter [get_clocks -of_objects [get_ports clk_system]] 0.1
