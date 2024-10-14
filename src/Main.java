import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n======= MENU =======");
            System.out.println("1) Run Pipeline");
            System.out.println("2) Compare Pipelines");
            System.out.println("0) Exit");
            System.out.print("Chose an Option: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> runPipelineWithUserInput();
                case 2 -> runPipelineComparison();
                case 0 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid Option!");
            }
        } while (choice != 0);
    }

    public static void runPipelineWithUserInput(){
        Pipeline pipeline = new Pipeline();

        Scanner scanner = new Scanner(System.in);
        boolean inputPrediction = true;
        boolean inputAuto = true;

        int[] memory = new int[128];
        memory[5] = -1;
        memory[6] = 10;
        memory[7] = 1;
        pipeline.setMemory(memory);
        pipeline.loadInstructions("teste.txt");

        System.out.println("Enter (Y) to trigger the prediction");
        String predictionInput = scanner.nextLine().trim().toUpperCase();

        if (!predictionInput.equals("Y")){
            inputPrediction = false;
        }

        System.out.println("Enter (M) to advance cycles manually");
        String userInput = scanner.nextLine().trim().toUpperCase();

        if (userInput.equals("M")) {
            inputAuto = false;
        }

        pipeline.runPipeline(inputPrediction,inputAuto);
    }

    public static void runPipelineComparison(){
        Pipeline predictionPipeline = new Pipeline();
        Pipeline nonPredictionPipeline = new Pipeline();

        int[] memory = new int[128];
        memory[5] = -1;
        memory[6] = 10;
        memory[7] = 1;

        predictionPipeline.setMemory(memory);
        nonPredictionPipeline.setMemory(memory);

        predictionPipeline.loadInstructions("teste.txt");
        nonPredictionPipeline.loadInstructions("teste.txt");

        predictionPipeline.runPipeline(true,true);

        System.out.println("\n\nRunning Second Pipeline...");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        nonPredictionPipeline.runPipeline(false,true);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\n===================== Performance Comparison =====================");
        System.out.printf("%-40s: %d%n", "Executed Instructions without Prediction", nonPredictionPipeline.executedInstruction);
        System.out.printf("%-40s: %d%n", "Executed Instructions with Prediction", predictionPipeline.executedInstruction);
        double executedDifference = 100 * ((double)(nonPredictionPipeline.executedInstruction - predictionPipeline.executedInstruction) / nonPredictionPipeline.executedInstruction);
        System.out.printf("%-40s: %.2f%%%n", "Improvement in Executed Instructions", executedDifference);

        System.out.println("-------------------------------------------------------------------");

        System.out.printf("%-40s: %d%n", "Invalid Instructions without Prediction", nonPredictionPipeline.invalidInstructions);
        System.out.printf("%-40s: %d%n", "Invalid Instructions with Prediction", predictionPipeline.invalidInstructions);
        double invalidDifference = 100 * ((double)(nonPredictionPipeline.invalidInstructions - predictionPipeline.invalidInstructions) / nonPredictionPipeline.invalidInstructions);
        System.out.printf("%-40s: %.2f%%%n", "Improvement of Invalid Instructions", invalidDifference);

        System.out.println("===================================================================");
    }

}