package br.com.martins.todolist.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.martins.todolist.api.dtos.requests.TodoCreateRequestDto;
import br.com.martins.todolist.api.dtos.requests.TodoUpdateRequestDto;
import br.com.martins.todolist.api.dtos.responses.TodoResponseDto;
import br.com.martins.todolist.api.dtos.responses.ApiResponseDto;
import br.com.martins.todolist.api.mappers.TodoMapper;
import br.com.martins.todolist.domain.entities.Todo;
import br.com.martins.todolist.domain.services.TodoService;
import br.com.martins.todolist.exceptions.SubjectNotInformedException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/todos")
public class TodoController {
  private final TodoService todoService;
  private final JwtDecoder jwtDecoder;

  public TodoController(TodoService todoService, JwtDecoder jwtDecoder) {
    this.todoService = todoService;
    this.jwtDecoder = jwtDecoder;
  }

  @PostMapping
  public ResponseEntity<ApiResponseDto<TodoResponseDto>> create(
      @RequestBody @Valid TodoCreateRequestDto todoCreateRequestDto,
      @RequestHeader("Authorization") String authorizationHeader) {
    String username = getSubjectInJWT(authorizationHeader);

    Todo todo = TodoMapper.toTodo(todoCreateRequestDto);
    TodoResponseDto todoResponseDto = TodoMapper.toTodoResponseDto(todoService.save(todo, username));
    ApiResponseDto<TodoResponseDto> apiResponseDto = new ApiResponseDto<>(HttpStatus.CREATED, "Todo criado com sucesso",
        todoResponseDto, null);
    return ResponseEntity.status(apiResponseDto.getStatus()).body(apiResponseDto);
  }

  @GetMapping
  public ResponseEntity<ApiResponseDto<List<TodoResponseDto>>> list(
      @RequestHeader("Authorization") String authorizationHeader) {
    String username = getSubjectInJWT(authorizationHeader);

    List<TodoResponseDto> todoResponseDtoList = TodoMapper.toTodoResponseDtoList(todoService.list(username));
    ApiResponseDto<List<TodoResponseDto>> apiResponseDto = new ApiResponseDto<>(HttpStatus.OK, "Lista de todos",
        todoResponseDtoList, null);
    return ResponseEntity.status(apiResponseDto.getStatus()).body(apiResponseDto);
  }

  @PutMapping("{id}")
  public ResponseEntity<ApiResponseDto<TodoResponseDto>> update(
      @PathVariable Long id,
      @RequestBody @Valid TodoUpdateRequestDto todoUpdateRequestDto,
      @RequestHeader("Authorization") String authorizationHeader) {
    String username = getSubjectInJWT(authorizationHeader);

    Todo todo = TodoMapper.toTodo(todoUpdateRequestDto);
    TodoResponseDto todoResponseDto = TodoMapper.toTodoResponseDto(todoService.update(id, todo, username));
    ApiResponseDto<TodoResponseDto> apiResponseDto = new ApiResponseDto<>(HttpStatus.OK, "Todo atualizado com sucesso",
        todoResponseDto, null);
    return ResponseEntity.status(apiResponseDto.getStatus()).body(apiResponseDto);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<ApiResponseDto<Void>> delete(
      @PathVariable("id") Long id,
      @RequestHeader("Authorization") String authorizationHeader) {
    String username = getSubjectInJWT(authorizationHeader);

    todoService.delete(id, username);
    ApiResponseDto<Void> apiResponseDto = new ApiResponseDto<>(HttpStatus.OK, "Todo deletado com sucesso", null,
        null);
    return ResponseEntity.status(apiResponseDto.getStatus()).body(apiResponseDto);
  }

  private String getSubjectInJWT(String authorizationHeader) {
    String token = authorizationHeader.substring(7); // Remove "Bearer "
    Jwt jwt = jwtDecoder.decode(token);

    if (jwt.getSubject() == null || jwt.getSubject().isEmpty()) {
      throw new SubjectNotInformedException("Subject não foi informado nas claims do Token");
    }

    return jwt.getSubject();
  }
}
