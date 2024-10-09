public class Instruction {

    String opCode;
    int oper1;
    int oper2;
    int oper3;
    int temp1;
    int temp2;
    int temp3;
    boolean valida;




    public Instruction(String opCode, int oper1, int oper2, int oper3) {
        this.opCode = opCode;
        this.oper1 = oper1;
        this.oper2 = oper2;
        this.oper3 = oper3;
        this.temp1 = 0;
        this.temp2 = 0;
        this.temp3 = 0;
        this.valida = true;
    }

    public Instruction(String opCode){
        this.opCode = opCode;
        this.valida = true;
    }

    @Override
    public String toString() {
        if(this.opCode.equals("HALT") || this.opCode.equals("NOOP")){
            return this.opCode;
        }
        return this.opCode + " " + oper1 + " " + oper2 + " " + oper3;
    }
}
