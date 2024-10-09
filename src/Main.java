public class Main {
    public static void main(String[] args) {

        Pipeline pipeline = new Pipeline();
        int[] memory = new int[128];
        memory[5] = -1;
        memory[6] = 10;
        memory[7] = 1;
        pipeline.setMemory(memory);
        pipeline.loadInstructions("teste.txt");
        pipeline.runPipeline();
    }
}