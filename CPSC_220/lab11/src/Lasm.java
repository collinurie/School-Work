

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;

/**
 * Written by: Collin Urie and AJ Perlino
 * An assembler for the "Lasm" assembly language for the Larc model computer.
 */
public class Lasm {
	
	static ArrayList<Label> labelCache = new ArrayList<Label>();

	
	public static void main(String[] args) {
		
		File programFile;
		if (args.length > 0)
			programFile = new File(args[0]);
		else {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select assembly language program file.");
			if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
				System.exit(0);
			programFile = chooser.getSelectedFile();
		}
		if (! (programFile.getName().endsWith(".s") || programFile.getName().endsWith(".S")) ) {
			System.out.println("***Error: File name must end with '.s'.");
			System.exit(0); // Might be needed to fully shut down if FileChooser was used.
			return;
		}
		Scanner in;
		try {
			in = new Scanner(programFile);
		}
		catch (FileNotFoundException e) {
			System.out.println("***Error: Can't open file for reading.");
			System.exit(0); 
			return;
		}
		ArrayList<ProgramItem> program;
		program = Parser.parse(in);
		in.close();
		if (program == null) {
			System.out.println("Could not parse " + programFile.getPath());
			System.exit(0); 
			return;
		}
		
		// FOR TESTING ONLY, print parsed program to standard output.
		addAddresses(program);
		//printProgram(program);
		ArrayList<String> mlProgram = codeGeneration(program);
		

		// Create a PrintWriter to write the machine language program
		String outputFileName = programFile.getName();
		int period = outputFileName.lastIndexOf('.');
		outputFileName = outputFileName.substring(0, period);  // strip off file extension
		outputFileName += ".out";  // add new file extension for ML program
		File outputFile = new File( programFile.getParentFile(), outputFileName);
		PrintWriter out;
		try {
			out = new PrintWriter(outputFile);
			for(String s : mlProgram) {
				out.println(s);
			}
			
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("File was successfully parsed, but could not write output file.");
		}
		
		
		System.exit(0);
	}
	

	/**
	 * Prints a Lasm program given as an ArrayList of ProgramItems to standard output.
	 */
	private static void printProgram(ArrayList<ProgramItem> program) {
		for (ProgramItem item: program) {
			if (item instanceof Label)
				System.out.println(item + ":");
			else
				System.out.println("    " + item);
		}
	}
	
	
	/**
	 * This will go through the code and assign addresses to the items in the program
	 * @param program
	 */
	private static void addAddresses(ArrayList<ProgramItem> program) {
		int adr = 0;
		// addresses each item in the array
		for(ProgramItem item: program) {
			if(item instanceof Label) {
				Label l = (Label)item;
				l.setAddress(adr);
				adr++;
				
				labelCache.add(l);
				
			}
			else if(item instanceof Directive) {
				Directive d = (Directive)item;
				
				String name= d.getName();
				String str = d.getStringValue();
				d.setAddress(adr);
				
				if(name.equals(".word")) {
					adr++;
				}
				else if(name.equals(".asciz")) {
					int len = str.length() +1;
					adr += len;
				}
				else if(name.equals(".space")) {
					int val = d.getIntValue();
					adr += val;
				}
				
			}
			else { // item is instruction
				Instruction ins = (Instruction)item;
				ins.setAddress(adr);
				String insName = ins.getName();
				
				if(insName.equals("la")){
					adr += 6;
					
				}
				else if(insName.equals("lbi")){
					adr += 6;
					
				}
				else if(insName.equals("lwa")){
					adr = 7;
					
				}
				else if(insName.equals("swa")){
					adr += 7;
					
				}
				else if(insName.equals("mov")){
					adr += 1;
				}
				else if(insName.equals("b")){
					adr += 7;
					
				}
				else if(insName.equals("bl")){
					adr += 7;
				}
				else if(insName.equals("ret")){
					adr += 1;
				}
				else if(insName.equals("push")){
					adr += 3;
				}
				else if(insName.equals("pop")){
					adr += 3;
				}
				else {
					adr++;
				}
				
			}// else
			
		}// end first pass
		
		// second pass through program checks to make sure labels exist
		for(ProgramItem p : program) {
			if(p instanceof Label) {
				Label lab = (Label)p;
				String insName = lab.getName();
				
				if(insName.equals("la") ||insName.equals("lwa")||insName.equals("swa")||insName.equals("b")
						||insName.equals("bl")||insName.equals("lwa")  ){					

					//String label = ins.getLabel();
					if(!labelCache.contains(lab)) {
						System.out.println("Label: "+lab.getName()+" does not exist.");
					}
				}

			}
		}// end for loop
		
		
	}// end addAddress
	
	/**
	 * This method will parse through the array of program items and build machine 
	 * language instructions 
	 * @param program
	 */
	public static ArrayList<String> codeGeneration(ArrayList<ProgramItem> program){
		ArrayList<String> mlProgram = new ArrayList<String>();
		String hexConvert = "0123456789abcdef";
		// parse through array generating code
		for(ProgramItem item : program) {
			if(item instanceof Label) {
				// find out what to do with label 
				Label l = (Label)item;
			}
			else if(item instanceof Directive) {
				// find out what to do with directive
				Directive d = (Directive)item;
				if(d.getName().equals(".asciz")) {
					mlProgram.add("#code for "+d.getName());

					String ascizVal = d.getStringValue();
					char [] charArr = new char[ascizVal.length()];
					charArr = ascizVal.toCharArray();
					
					for(int i = 0; i < charArr.length; i++) {
						String value = Integer.toHexString((int)charArr[i]);
						if(value.length() == 1)
							value = "000"+value;
						if(value.length() == 2)
							value = "00"+value;
						if(value.length() == 3)
							value = "0"+value;
						
						value = "0x"+value;
						mlProgram.add(value);
						mlProgram.add("0x0000");
					}
					
					
					
					
				}
				if(d.getName().equals(".word")) {
					mlProgram.add("#code for "+d.getName());
					int value = d.getIntValue();
					String strVal = Integer.toHexString(value);
					if(strVal.length() == 1)
						strVal = "000"+strVal;
					if(strVal.length() == 2)
						strVal = "00"+strVal;
					if(strVal.length() == 3)
						strVal = "0"+strVal;
					if(strVal.length() > 4)
						strVal = strVal.substring(strVal.length()-4);
					mlProgram.add(strVal);
				}
				if(d.getName().equals(".space")) {
					mlProgram.add("#code for "+d.getName());
					int spaces = d.getIntValue();
					for(int i = 0; i < spaces; i++) {
						mlProgram.add("0x0000");
					}
				}
				
			}
			else {
				// generate code for instructions
				Instruction i = (Instruction)item;
				
				String mlins; 
				int ra = i.getRa();
				int rb = i.getRb();
				int rc = i.getRc();
				int limm = i.getLimm();
				int simm = i.getSimm();
				int labelAddr = 0;
				int bimm = i.getBimm();
				
				
				String labelAddrStr= "";
				String limmStr = Integer.toHexString(limm);
				String bimmStr = Integer.toHexString(bimm);
				
				if(limm < 0)
					limmStr = limmStr.substring(limmStr.length()-2);
				if(limmStr.length() == 1)
					limmStr = "0"+ limmStr;
				
				String insLabel = i.getLabel();
				//System.out.println(insLabel);
				if(insLabel != null) {
					for(Label l : labelCache) {
						if(l.getName().equals(insLabel))
							labelAddr = l.getAddress();
							labelAddrStr = Integer.toHexString(labelAddr);
					}
				}
				
				// makes sure Strings have the propper length to be implemented
				if(labelAddrStr.length() == 1)
					labelAddrStr = "000"+labelAddrStr;
				if(labelAddrStr.length() == 2)
					labelAddrStr = "00"+labelAddrStr;
				if(labelAddrStr.length() == 3)
						labelAddrStr = "0"+labelAddrStr;
				if(labelAddrStr.length() > 4)
					labelAddrStr = labelAddrStr.substring(labelAddrStr.length()-4);
				
				if(bimmStr.length() == 1)
					bimmStr = "000"+bimmStr;
				if(bimmStr.length() == 2)
					bimmStr = "00"+bimmStr;
				if(bimmStr.length() == 3)
					bimmStr = "0"+bimmStr;
				if(bimmStr.length() > 4)
					bimmStr = bimmStr.substring(bimmStr.length()-4);
				//System.out.println(limStr);
				
				
				
				if(i.getName().equals("la")) {
					mlProgram.add("#code for "+i.getName());

					String ins1 = "0x8"+hexConvert.charAt(ra)+labelAddrStr.substring(2);
					String ins2 = "0x8c08";
					String ins3 = "0x4"+hexConvert.charAt(ra)+hexConvert.charAt(ra)+"c";
					String ins4 = "0x5"+hexConvert.charAt(ra)+hexConvert.charAt(ra)+"c";
					String ins5 = "0x9c"+labelAddrStr.substring(0,2);
					String ins6 = "0x0"+hexConvert.charAt(ra)+hexConvert.charAt(ra)+"c";
					mlProgram.add(ins1);
					mlProgram.add(ins2);
					mlProgram.add(ins3);
					mlProgram.add(ins4);
					mlProgram.add(ins5);
					mlProgram.add(ins6);
					
				}
				else if(i.getName().equals("lbi")) {
					mlProgram.add("#code for "+i.getName());
					
					String ins1 = "0x8"+hexConvert.charAt(ra)+bimmStr.substring(2);
					String ins2 = "0x8c08";
					String ins3 = "0x4"+hexConvert.charAt(ra)+hexConvert.charAt(ra)+"c";
					String ins4 = "0x5"+hexConvert.charAt(ra)+hexConvert.charAt(ra)+"c";
					String ins5 = "0x9c"+bimmStr.substring(0,2);
					String ins6 = "0x0"+hexConvert.charAt(ra)+hexConvert.charAt(ra)+"c";
					mlProgram.add(ins1);
					mlProgram.add(ins2);
					mlProgram.add(ins3);
					mlProgram.add(ins4);
					mlProgram.add(ins5);
					mlProgram.add(ins6);

					
				}
				else if(i.getName().equals("lwa")) {
					mlProgram.add("#code for "+i.getName());
					
					String ins1 = "0x8d"+labelAddrStr.substring(2);
					String ins2 = "0x8c08";
					String ins3 = "0x4ddc";
					String ins4 = "0x5ddc";
					String ins5 = "0x9c"+labelAddrStr.substring(0,2);
					String ins6 = "0x0ddc";
					String ins7 = "0xc"+hexConvert.charAt(ra)+"d0";
					mlProgram.add(ins1);
					mlProgram.add(ins2);
					mlProgram.add(ins3);
					mlProgram.add(ins4);
					mlProgram.add(ins5);
					mlProgram.add(ins6);
					mlProgram.add(ins7);
					
				}
				else if(i.getName().equals("swa")) {
					mlProgram.add("#code for "+i.getName());
					
					String ins1 = "0x8d"+labelAddrStr.substring(2);
					String ins2 = "0x8c08";
					String ins3 = "0x4ddc";
					String ins4 = "0x5ddc";
					String ins5 = "0x9c"+labelAddrStr.substring(0,2);
					String ins6 = "0x0ddc";
					String ins7 = "0xd"+hexConvert.charAt(ra)+"d0";
					mlProgram.add(ins1);
					mlProgram.add(ins2);
					mlProgram.add(ins3);
					mlProgram.add(ins4);
					mlProgram.add(ins5);
					mlProgram.add(ins6);
					mlProgram.add(ins7);
				}
				else if(i.getName().equals("mov")) {
					mlProgram.add("#code for "+i.getName());
					
					String ins1 = "0x0"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+"0";
					mlProgram.add(ins1);

	
				}
				else if(i.getName().equals("b")) {
					mlProgram.add("#code for "+i.getName());
					
					String ins1 = "0x8d"+labelAddrStr.substring(2);
					String ins2 = "0x8c08";
					String ins3 = "0x4ddc";
					String ins4 = "0x5ddc";
					String ins5 = "0x9c"+labelAddrStr.substring(0,2);
					String ins6 = "0x0ddc";
					String ins7 = "0xe0d0";
					mlProgram.add(ins1);
					mlProgram.add(ins2);
					mlProgram.add(ins3);
					mlProgram.add(ins4);
					mlProgram.add(ins5);
					mlProgram.add(ins6);
					mlProgram.add(ins7);
	
				}
				else if(i.getName().equals("bl")) {
					mlProgram.add("#code for "+i.getName());
					
					String ins1 = "0x8d"+labelAddrStr.substring(2);
					String ins2 = "0x8c08";
					String ins3 = "0x4ddc";
					String ins4 = "0x5ddc";
					String ins5 = "0x9c"+labelAddrStr.substring(0,2);
					String ins6 = "0x0ddc";
					String ins7 = "0xebd0";
					mlProgram.add(ins1);
					mlProgram.add(ins2);
					mlProgram.add(ins3);
					mlProgram.add(ins4);
					mlProgram.add(ins5);
					mlProgram.add(ins6);
					mlProgram.add(ins7);
	
				}
				else if(i.getName().equals("ret")) {
					mlProgram.add("#code for "+i.getName());
					
					String ins1 = "0xd0b0";
					mlProgram.add(ins1);
				}
				else if(i.getName().equals("push")) {
					mlProgram.add("#code for "+i.getName());
					
					String ins1 = "0x8c01";
					String ins2 = "0x1aac";
					String ins3 = "0xd"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(simm);
					mlProgram.add(ins1);
					mlProgram.add(ins2);
					mlProgram.add(ins3);
				}
				else if(i.getName().equals("pop")) {
					mlProgram.add("#code for "+i.getName());
					
					String ins1 = "0xc"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(10);
					String ins2 = "0x8c01";
					String ins3 = "0x0aac";
					mlProgram.add(ins1);
					mlProgram.add(ins2);
					mlProgram.add(ins3);
					
				}
				else if(i.getName().equals("beqz")) {
					
					int gap = i.getAddress()-labelAddr;
					String gapStr = Integer.toHexString(gap);
					if(gap < 0)
						gapStr = gapStr.substring(gapStr.length()-2);
					if(gapStr.length() == 1)
						gapStr = "0"+ gapStr;
					
					mlins = "0xa"+hexConvert.charAt(ra)+ gapStr;
					mlProgram.add(mlins);
					
				}
				else if(i.getName().equals("bnez")) {
									
					int gap = i.getAddress()-labelAddr;
					String gapStr = Integer.toHexString(gap);
					if(gap < 0)
						gapStr = gapStr.substring(gapStr.length()-2);
					if(gapStr.length() == 1)
						gapStr = "0"+ gapStr;
			
					mlins = "0xb"+hexConvert.charAt(ra)+ gapStr;
					mlProgram.add(mlins);
		
				}
				else if(i.getName().equals("add")) {
					mlins = "0x0"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(rc);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("sub")) {
					mlins = "0x1"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(rc);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("mul")) {
					mlins = "0x2"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(rc);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("div")) {
					mlins = "0x3"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(rc);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("sll")) {
					mlins = "0x4"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(rc);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("srl")) {
					mlins = "0x5"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(rc);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("nor")) {
					mlins = "0x6"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(rc);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("slt")) {
					mlins = "0x7"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(rc);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("li")) {
					mlins = "0x8"+hexConvert.charAt(ra)+limmStr;
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("lui")) {
					mlins = "0x9"+hexConvert.charAt(ra)+limmStr;
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("lw")) {
					mlins = "0xc"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(simm);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("sw")) {
					mlins = "0xd"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+hexConvert.charAt(simm);
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("jalr")) {
					mlins = "0xe"+hexConvert.charAt(ra)+hexConvert.charAt(rb)+"0";
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
				else if(i.getName().equals("syscall")) {
					mlins = "0xf000";
					//System.out.println(mlins);
					mlProgram.add(mlins);

				}
			}// end instructuion else
			
			
			
		}// end for each 
		
//		for(String s: mlProgram) {
//			System.out.println(s);
//		}
		
		
		return mlProgram;
	}

	

}
