package com.example.demo.dto;

public class MoveResultDto {
    private SimpleMoveDto move;
    private String result;
    private String winner;
    private String finalBoard;

    public MoveResultDto() {}

    public SimpleMoveDto getMove() { return move; }
    public void setMove(SimpleMoveDto move) { this.move = move; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public String getFinalBoard() { return finalBoard; }
    public void setFinalBoard(String finalBoard) { this.finalBoard = finalBoard; }
}
