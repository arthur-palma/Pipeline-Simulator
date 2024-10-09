import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Pipeline {

    private int[] registers = new int[32];

    private int[] memory = new int[128];
    private int pc = 0;
    private ArrayList<Instruction> instructions = new ArrayList<>();

    private boolean[] predictionTable = new boolean[128];
    private boolean predictionEnabled = false;


    private boolean halt = false;
    private boolean changePc = false;
    private boolean invalidateInstructions = false;
    private int newPc;

    private Instruction FETCH = null;
    private Instruction DECODE = null;
    private Instruction EXECUTE = null;
    private Instruction MEMORYACCESS = null;
    private Instruction WRITEBACK = null;

    public void setMemory(int[] memory){
        this.memory = memory;
    }

    public void loadInstructions(String txtName){
        try(BufferedReader br = new BufferedReader(new FileReader(txtName))) {
            String line;
            while((line = br.readLine()) != null && !line.equals("")) {
                String[] parts = line.trim().split(" ");
                String opCode = parts[0];
                if(!opCode.equals("NOOP") && !opCode.equals("HALT")){
                    int oper1 = Integer.parseInt(parts[1]);
                    int oper2 = Integer.parseInt(parts[2]);
                    int oper3 = Integer.parseInt(parts[3]);
                    instructions.add(new Instruction(opCode,oper1,oper2,oper3));
                }
                else{
                    instructions.add(new Instruction(opCode));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void instructionFetch() {
        if( pc < instructions.size()){
            FETCH = instructions.get(pc);
            FETCH.temp3 = pc;

            if(FETCH.opCode.equals("BEQ") && predictionEnabled){
                boolean branchTaken = predictionTable[pc];
                changePc = true;
                newPc = branchTaken ? pc + FETCH.oper3 : pc + 1;
            }
            else {
                pc++;
            }
        }
        else{
            FETCH = null;
        }
    }


    public void decode(){
            DECODE = FETCH;
    }

    public void execute(){
        if(DECODE != null && DECODE.valida){
            switch (DECODE.opCode) {
                case "ADD" -> {
                    DECODE.temp1 = registers[DECODE.oper2] + registers[DECODE.oper3];
                }
                case "SUB" -> {
                    DECODE.temp1 = registers[DECODE.oper2] - registers[DECODE.oper3];
                }
                case "BEQ" -> {
                    boolean branchTaken = registers[DECODE.oper1] == registers[DECODE.oper2];

                    if (branchTaken) {
                        if(!predictionEnabled){
                            changePc = true;
                            invalidateInstructions = true;
                            newPc = DECODE.temp3 + DECODE.oper3;
                        }
                        else{
                            if(branchTaken != predictionTable[DECODE.temp3 % predictionTable.length]){
                                changePc = true;
                                invalidateInstructions = true;
                                newPc = DECODE.temp3 + DECODE.oper3;
                            }
                            predictionTable[DECODE.temp3 % predictionTable.length] = branchTaken;
                        }
                    }

                }
                case "LW" -> {
                    DECODE.temp1 = registers[DECODE.oper2] + DECODE.oper3;
                }
                case "SW" -> DECODE.temp1 = registers[DECODE.oper2] + DECODE.oper3;
            }
        }
        EXECUTE = DECODE;
    }

    public void memoryAccess(){
        if(EXECUTE != null && EXECUTE.valida){
            switch (EXECUTE.opCode){
                case "LW" ->{
                    EXECUTE.temp2 = memory[EXECUTE.temp1];
                }
                case "SW" ->{
                    memory[EXECUTE.temp1] = registers[EXECUTE.oper1];
                }
            }
        }
        MEMORYACCESS = EXECUTE;
    }

    public void writeBack(){
        if(MEMORYACCESS != null && MEMORYACCESS.valida){
            switch (MEMORYACCESS.opCode) {
                case "ADD", "SUB" -> {
                    if (MEMORYACCESS.oper1 != 0)
                        registers[MEMORYACCESS.oper1] = MEMORYACCESS.temp1;
                }
                case "LW" -> {
                    if(MEMORYACCESS.oper1 != 0)
                        registers[MEMORYACCESS.oper1] = MEMORYACCESS.temp2;
                }
                case "HALT" -> halt = true;
            }
        }
        WRITEBACK = MEMORYACCESS;
    }

    public void printPipelineState(int cycles) {
        System.out.println();
        System.out.printf("%-12s: %s%n", "FETCH", (FETCH != null ? FETCH : "null"));
        System.out.printf("%-12s: %s%n", "DECODE", (DECODE != null ? DECODE : "null"));
        System.out.printf("%-12s: %s%n", "EXECUTE", (EXECUTE != null ? EXECUTE : "null"));
        System.out.printf("%-12s: %s%n", "MEMORY", (MEMORYACCESS != null ? MEMORYACCESS : "null"));
        System.out.printf("%-12s: %s%n", "WRITEBACK", (WRITEBACK != null ? WRITEBACK : "null"));

        System.out.println("\n......................... REGISTERS .........................");
        for (int i = 0; i < registers.length; i++) {
            System.out.printf("R%-2d: %-8d", i, registers[i]);
            if ((i + 1) % 4 == 0) {
                System.out.println();
            }
        }

        if(predictionEnabled){
            System.out.println("\n~~~~~~~~~~~~~~~ PREDICTION TABLE (PC -> Pred) ~~~~~~~~~~~~~~~");
            for (int i = 0; i < predictionTable.length; i++) {
                if (instructions.size() > i && instructions.get(i).opCode.equals("BEQ")) {
                    System.out.printf("PC: %-3d | Prediction: %-5s%n", i, predictionTable[i] ? "Taken" : "Not Taken");
                }
            }
        }
        System.out.println("=============================================================");
    }

    public void runPipeline() {
        int cycles = 1;
        Scanner scanner = new Scanner(System.in);
        boolean pass = true;
        boolean auto = true;

        System.out.println("Digite (S) caso queira acionar a predição de desvio?");
        String predictionInput = scanner.nextLine().trim().toUpperCase();

        if (predictionInput.equals("S")){
            predictionEnabled = true;
        }

        System.out.println("Deseja avançar os ciclos automaticamente (A) ou manualmente (M)?");
        String userInput = scanner.nextLine().trim().toUpperCase();


        if (userInput.equals("M")) {
            auto = false;
            pass = false;
        }

        while (!halt) {

            System.out.println("\n===================== PIPELINE STATE ======================");
            System.out.println("CYCLE : " + cycles + " | PC : " + pc);

            writeBack();
            memoryAccess();
            execute();
            decode();
            instructionFetch();

            printPipelineState(cycles);

            if(changePc){
                pc = newPc;
                changePc = false;
                if(invalidateInstructions){
                    FETCH.valida = false;
                    DECODE.valida = false;
                    invalidateInstructions = false;
                }
            }

            cycles++;

            if (!pass) {
                System.out.println("Pressione qualquer tecla para avançar para o próximo ciclo...");
                scanner.nextLine();
            }

            if(!auto){
                pass = false;
            }

        }
    }
}
