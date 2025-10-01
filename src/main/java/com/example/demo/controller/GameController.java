package com.example.demo.controller;

import com.example.demo.dto.BoardDto;
import com.example.demo.dto.MoveResultDto;
import com.example.demo.engine.GameEngine; // <-- проверь, что GameEngine находится в этом пакете

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameEngine engine = new GameEngine();

    @PostMapping("/{rules}/nextMove")
    public ResponseEntity<MoveResultDto> nextMove(
            @PathVariable("rules") String rules,
            @RequestBody BoardDto boardDto) {


        MoveResultDto res = engine.computeNextMove(boardDto);
        return ResponseEntity.ok(res);
    }
}
