`timescale 1ns / 1ps

module DefaultExampleTestbench
  ();
   
   localparam CLK_PERIOD = 10;    // テスト用暫定値
   localparam CLK_MAX_COUNT = 100;

   logic clock = 1;
   logic reset = 1;

   logic io_success;
   TestHarness dut(.*);
   
   /**
    * システム・クロックを生成します。
    */
   task genClock();
       forever
        #(CLK_PERIOD / 2) clock = ~clock;
    endtask // genClk
      
   initial
     begin
        /* クロック生成 */
        fork
           genClock();
        join_none
        
        repeat(3) @(posedge clock);
        reset <= 0;
        repeat(3) @(posedge clock);

	repeat(CLK_MAX_COUNT) @(posedge clock);
	$finish;
	
     end // initial begin
   
endmodule
