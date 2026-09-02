package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.GenreDto;
import com.example.WarmTea.Service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<List<GenreDto>> getAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    @PostMapping
    public ResponseEntity<GenreDto> createGenre(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        GenreDto dto = genreService.createGenre(name);
        return ResponseEntity.ok(dto);
    }
}