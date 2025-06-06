package com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mongodb.service;

import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.exception.CharacterNotFoundException;
import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mongodb.dto.CharacterUpdateRequest;
import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mongodb.entity.Character;
import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mongodb.entity.Piece;
import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mongodb.enums.Power;
import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mongodb.repository.BuildRepository;
import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mongodb.repository.CharacterRepository;
import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mongodb.repository.PieceRepository;
import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mysql.entity.User;
import com.examplecom.ezequiel.itacademy.brawlarena_back.brawlarena.mysql.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterServiceImplTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BuildRepository buildRepository;

    @Mock
    private PieceRepository pieceRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CharacterServiceImpl characterService;


    // Método helper para crear Characters de prueba
    private Character createTestCharacter(String id) {
        return new Character(
                id,
                "Test-" + id,
                "Descripción",
                "Medium",
                new ArrayList<>(),
                new ArrayList<>(),
                "image.png",
                0,
                "test-game.png"
        );

    }

    @Test
    void getAllCharacters_ReturnsFluxOfCharacters() {

        Character char1 = createTestCharacter("1");
        Character char2 = createTestCharacter("2");

        when(characterRepository.findAll())
                .thenReturn(Flux.just(char1, char2));

        StepVerifier.create(characterService.getAllCharacters())
                .expectNext(char1)
                .expectNext(char2)
                .verifyComplete();
    }

    @Test
    void getAllCharacters_ReturnsEmptyFluxWhenNoCharacters() {

        when(characterRepository.findAll())
                .thenReturn(Flux.empty());

        StepVerifier.create(characterService.getAllCharacters())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getAllCharacters_PropagatesRepositoryError() {

        RuntimeException simulatedError = new RuntimeException("Error en MongoDB");
        when(characterRepository.findAll())
                .thenReturn(Flux.error(simulatedError));

        StepVerifier.create(characterService.getAllCharacters())
                .expectErrorMatches(ex -> {
                    assertThat(ex).isInstanceOf(RuntimeException.class);
                    assertThat(ex.getMessage()).isEqualTo("Error en MongoDB");
                    return true;
                })
                .verify();
    }

    @Test
    void getUnlockedCharacters_ReturnsFilteredCharacters() {
        String playerId = "player1";
        User mockUser = new User();
        mockUser.setNickname(playerId);
        mockUser.setCharacterIds("[1,3]");

        Character char1 = createTestCharacter("1"); // desbloqueado
        Character char2 = createTestCharacter("2"); // bloqueado
        Character char3 = createTestCharacter("3"); // desbloqueado

        when(userRepository.findByNickname(playerId)).thenReturn(Mono.just(mockUser));
        when(characterRepository.findAll()).thenReturn(Flux.just(char1, char2, char3));

        StepVerifier.create(characterService.getUnlockedCharacters(playerId))
                .expectNextMatches(c -> c.getId().equals("1"))
                .expectNextMatches(c -> c.getId().equals("3"))
                .expectComplete()
                .verify();
    }

    @Test
    void getUnlockedCharacters_ReturnsEmptyWhenNoMatches() {

        User mockUser = new User();
        mockUser.setNickname("player1");
        mockUser.setCharacterIds("[]");

        when(userRepository.findByNickname("player1"))
                .thenReturn(Mono.just(mockUser));

        when(characterRepository.findAll())
                .thenReturn(Flux.empty());

        StepVerifier.create(characterService.getUnlockedCharacters("player1"))
                .verifyComplete();
    }

    @Test
    void getUnlockedCharacters_PropagatesErrors() {

        User mockUser = new User();
        mockUser.setNickname("player1");
        mockUser.setCharacterIds("[]");

        when(userRepository.findByNickname("player1"))
                .thenReturn(Mono.just(mockUser));

        when(characterRepository.findAll())
                .thenReturn(Flux.error(new RuntimeException("DB Error")));

        StepVerifier.create(characterService.getUnlockedCharacters("player1"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void unlockCharacter_SuccessfullyUnlocks_ReturnsTrue() {

        BuildService buildService = mock(BuildService.class);

        Character lockedChar = createTestCharacter("char1");
        Character savedChar = createTestCharacter("char1");

        User testUser = User.builder()
                .nickname("player1")
                .tokens(100)
                .characterIds("[]")
                .build();

        when(characterRepository.findById("char1")).thenReturn(Mono.just(lockedChar));
        when(userRepository.findByNickname("player1")).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        CharacterServiceImpl characterService = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(characterService.unlockCharacter("player1", "char1"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void unlockCharacter_AlreadyUnlocked_ReturnsFalse() {

        Character unlockedChar = createTestCharacter("char1");
        User testUser = User.builder()
                .nickname("player1")
                .tokens(100)
                .characterIds("[\"char1\"]")
                .build();

        when(characterRepository.findById("char1"))
                .thenReturn(Mono.just(unlockedChar));
        when(userRepository.findByNickname("player1"))
                .thenReturn(Mono.just(testUser));

        // Ejecuta y verifica
        StepVerifier.create(characterService.unlockCharacter("player1", "char1"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void unlockCharacter_NotFound_ThrowsException() {
        when(characterRepository.findById("char1"))
                .thenReturn(Mono.empty());
        when(userRepository.findByNickname("player1"))
                .thenReturn(Mono.just(User.builder().build()));

        StepVerifier.create(characterService.unlockCharacter("player1", "char1"))
                .expectErrorMatches(ex -> ex instanceof CharacterNotFoundException &&
                        ex.getMessage().equals("Personaje no encontrado"))
                .verify();
    }

    @Test
    void unlockCharacter_RepositoryError_PropagatesError() {

        when(userRepository.findByNickname("player1"))
                .thenReturn(Mono.just(User.builder().build()));

        when(characterRepository.findById("char1"))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));

        StepVerifier.create(characterService.unlockCharacter("player1", "char1"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getCharacterDetail_ReturnsCharacterWhenExists() {

        Character testChar = createTestCharacter("char1");
        when(characterRepository.findById("char1"))
                .thenReturn(Mono.just(testChar));

        StepVerifier.create(characterService.getCharacterDetail("char1"))
                .expectNextMatches(character ->
                        character.getId().equals("char1") &&
                                character.getName().equals("Test-char1"))
                .verifyComplete();
    }

    @Test
    void getCharacterDetail_ThrowsWhenCharacterNotFound() {

        when(characterRepository.findById("char1"))
                .thenReturn(Mono.empty());

        StepVerifier.create(characterService.getCharacterDetail("char1"))
                .expectErrorMatches(ex ->
                        ex instanceof CharacterNotFoundException &&
                                ex.getMessage().equals("Personaje no encontrado"))
                .verify();
    }

    @Test
    void getCharacterDetail_PropagatesRepositoryErrors() {

        when(characterRepository.findById("char1"))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));

        StepVerifier.create(characterService.getCharacterDetail("char1"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void updateCharacter_conCharacterValidoYSinPiezas_actualizaCamposYGuardaCorrectamente() {
        String characterId = "char1";

        Character personajeExistente = new Character();
        personajeExistente.setId(characterId);
        personajeExistente.setName("ViejoNombre");
        personajeExistente.setDescription("ViejaDesc");
        personajeExistente.setDifficulty("Fácil");
        personajeExistente.setImageUrl("vieja.png");
        personajeExistente.setPowers(new ArrayList<>());

        CharacterUpdateRequest request = new CharacterUpdateRequest();
        request.setName("NuevoNombre");
        request.setDescription("NuevaDesc");
        request.setDifficulty("Difícil");
        request.setImageUrl("nueva.png");
        request.setPowers(List.of(Power.POSTURA_IMPARABLE));
        request.setPieces(null);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(personajeExistente));

        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        BuildService buildService = mock(BuildService.class);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.updateCharacter(characterId, request))
                .expectNextMatches(actualizado ->
                        actualizado.getName().equals("NuevoNombre") &&
                                actualizado.getDescription().equals("NuevaDesc") &&
                                actualizado.getDifficulty().equals("Difícil") &&
                                actualizado.getImageUrl().equals("nueva.png") &&
                                actualizado.getPowers().equals(List.of(Power.POSTURA_IMPARABLE)) &&
                                actualizado.getPieces() == null)

                .verifyComplete();
    }

    @Test
    void updateCharacter_conCharacterValidoYConPiezas_actualizaCamposYLimpiaCache() {
        String characterId = "char1";

        Character personajeExistente = new Character();
        personajeExistente.setId(characterId);
        personajeExistente.setPowers(new ArrayList<>());

        CharacterUpdateRequest request = new CharacterUpdateRequest();
        request.setName("NuevoNombre");
        request.setDescription("NuevaDesc");
        request.setDifficulty("Difícil");
        request.setImageUrl("nueva.png");
        request.setPowers(List.of(Power.POSTURA_IMPARABLE));

        Piece pieza = new Piece();
        pieza.setId("p1");
        request.setPieces(List.of(pieza));

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(personajeExistente));

        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        BuildService buildService = mock(BuildService.class);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.updateCharacter(characterId, request))
                .expectNextMatches(actualizado ->
                        actualizado.getName().equals("NuevoNombre") &&
                                actualizado.getDescription().equals("NuevaDesc") &&
                                actualizado.getDifficulty().equals("Difícil") &&
                                actualizado.getImageUrl().equals("nueva.png") &&
                                actualizado.getPowers().equals(List.of(Power.POSTURA_IMPARABLE)) &&
                                actualizado.getPieces().equals(List.of(pieza)))
                .verifyComplete();

        verify(buildService).clearPiecesCache(characterId);
    }

    @Test
    void updateCharacter_personajeNoExiste_lanzaCharacterNotFoundException() {
        String characterId = "charInexistente";

        CharacterUpdateRequest request = new CharacterUpdateRequest();
        request.setName("Nombre");
        request.setDescription("Desc");
        request.setDifficulty("Media");
        request.setImageUrl("img.png");
        request.setPowers(List.of(Power.POSTURA_IMPARABLE));
        request.setPieces(null);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.empty());

        BuildService buildService = mock(BuildService.class);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.updateCharacter(characterId, request))
                .expectErrorMatches(error ->
                        error instanceof CharacterNotFoundException &&
                                error.getMessage().contains("Personaje no encontrado"))
                .verify();
    }

    @Test
    void updateCharacter_conRequestNulo_lanzaNullPointerException() {
        String characterId = "char1";

        BuildService buildService = mock(BuildService.class);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        assertThrows(NullPointerException.class,
                () -> service.updateCharacter(characterId, null).block());
    }

    @Test
    void updateCharacter_conPiezasNulas_noInvocaClearPiecesCachePeroGuarda() {
        String characterId = "char1";

        Character personajeExistente = new Character();
        personajeExistente.setId(characterId);
        personajeExistente.setPowers(new ArrayList<>());

        CharacterUpdateRequest request = new CharacterUpdateRequest();
        request.setName("NombreActualizado");
        request.setDescription("DescActualizada");
        request.setDifficulty("Alta");
        request.setImageUrl("nueva.png");
        request.setPowers(List.of(Power.POSTURA_IMPARABLE));
        request.setPieces(null);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(personajeExistente));

        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        BuildService buildService = mock(BuildService.class);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.updateCharacter(characterId, request))
                .expectNextMatches(actualizado ->
                        actualizado.getName().equals("NombreActualizado") &&
                                actualizado.getDescription().equals("DescActualizada") &&
                                actualizado.getDifficulty().equals("Alta") &&
                                actualizado.getImageUrl().equals("nueva.png") &&
                                actualizado.getPowers().equals(List.of(Power.POSTURA_IMPARABLE)) &&
                                actualizado.getPieces() == null)
                .verifyComplete();

        verify(buildService, never()).clearPiecesCache(any());
    }

    @Test
    void updateCharacter_errorAlBuscarPersonaje_propagaExcepcion() {
        String characterId = "char1";

        CharacterUpdateRequest request = new CharacterUpdateRequest();
        request.setName("Nombre");
        request.setDescription("Desc");
        request.setDifficulty("Media");
        request.setImageUrl("img.png");
        request.setPowers(List.of(Power.POSTURA_IMPARABLE));
        request.setPieces(null);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.error(new RuntimeException("Error al buscar")));

        BuildService buildService = mock(BuildService.class);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.updateCharacter(characterId, request))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error al buscar"))
                .verify();
    }

    @Test
    void updateCharacter_errorAlGuardarPersonaje_propagaExcepcion() {
        String characterId = "char1";

        Character personajeExistente = new Character();
        personajeExistente.setId(characterId);
        personajeExistente.setPowers(new ArrayList<>());

        CharacterUpdateRequest request = new CharacterUpdateRequest();
        request.setName("Nombre");
        request.setDescription("Desc");
        request.setDifficulty("Alta");
        request.setImageUrl("img.png");
        request.setPowers(List.of(Power.POSTURA_IMPARABLE));
        request.setPieces(null);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(personajeExistente));

        when(characterRepository.save(any(Character.class)))
                .thenReturn(Mono.error(new RuntimeException("Error al guardar")));

        BuildService buildService = mock(BuildService.class);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.updateCharacter(characterId, request))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error al guardar"))
                .verify();
    }

    @Test
    void updateCharacter_invocaClearPiecesCacheSoloSiHayPiezas() {
        String characterId = "char1";

        Character personajeExistente = new Character();
        personajeExistente.setId(characterId);
        personajeExistente.setPowers(new ArrayList<>());

        CharacterUpdateRequest requestConPiezas = new CharacterUpdateRequest();
        requestConPiezas.setName("Nombre");
        requestConPiezas.setDescription("Desc");
        requestConPiezas.setDifficulty("Media");
        requestConPiezas.setImageUrl("img.png");
        requestConPiezas.setPowers(List.of(Power.POSTURA_IMPARABLE));

        Piece pieza = new Piece();
        pieza.setId("pieza1");
        requestConPiezas.setPieces(List.of(pieza));

        CharacterUpdateRequest requestSinPiezas = new CharacterUpdateRequest();
        requestSinPiezas.setName("Nombre");
        requestSinPiezas.setDescription("Desc");
        requestSinPiezas.setDifficulty("Media");
        requestSinPiezas.setImageUrl("img.png");
        requestSinPiezas.setPowers(List.of(Power.POSTURA_IMPARABLE));
        requestSinPiezas.setPieces(null);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(personajeExistente));

        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        BuildService buildService = mock(BuildService.class);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.updateCharacter(characterId, requestConPiezas))
                .expectNextCount(1)
                .verifyComplete();
        verify(buildService).clearPiecesCache(characterId);

        reset(buildService);

        StepVerifier.create(service.updateCharacter(characterId, requestSinPiezas))
                .expectNextCount(1)
                .verifyComplete();
        verify(buildService, never()).clearPiecesCache(any());
    }

    @Test
    void assignPieces_conListaDePieceIdsNula_lanzaIllegalArgumentException() {
        String characterId = "char1";

        assertThrows(IllegalArgumentException.class,
                () -> characterService.assignPieces(characterId, null).block());
    }

    @Test
    void assignPieces_conListaDePieceIdsVacia_lanzaIllegalArgumentException() {
        String characterId = "char1";

        assertThrows(IllegalArgumentException.class,
                () -> characterService.assignPieces(characterId, List.of()).block());
    }

    @Test
    void assignPieces_personajeNoExiste_lanzaCharacterNotFoundException() {
        String characterId = "charInexistente";
        List<String> pieceIds = List.of("pieza1", "pieza2");

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.empty());

        StepVerifier.create(characterService.assignPieces(characterId, pieceIds))
                .expectErrorMatches(error ->
                        error instanceof CharacterNotFoundException &&
                                error.getMessage().contains("Personaje no encontrado"))
                .verify();
    }

    @Test
    void assignPieces_conCharacterYPieceIdsValidos_guardaPersonajeConPiezas() {
        String characterId = "char1";
        String pieceId1 = "pieza1";
        String pieceId2 = "pieza2";
        List<String> pieceIds = List.of(pieceId1, pieceId2);

        Piece pieza1 = new Piece();
        pieza1.setId(pieceId1);

        Piece pieza2 = new Piece();
        pieza2.setId(pieceId2);

        Character personajeExistente = new Character();
        personajeExistente.setId(characterId);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(personajeExistente));

        when(pieceRepository.findByIdIn(pieceIds))
                .thenReturn(Flux.just(pieza1, pieza2));

        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(characterService.assignPieces(characterId, pieceIds))
                .expectNextMatches(actualizado ->
                        actualizado.getId().equals(characterId) &&
                                actualizado.getPieces().equals(List.of(pieza1, pieza2)))
                .verifyComplete();
    }

    @Test
    void assignPieces_conListaDePiezasVacia_guardaPersonajeSinPiezas() {
        String characterId = "char1";
        List<String> pieceIds = List.of("pieza1", "pieza2");

        Character personajeExistente = new Character();
        personajeExistente.setId(characterId);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(personajeExistente));

        when(pieceRepository.findByIdIn(pieceIds))
                .thenReturn(Flux.empty());

        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(characterService.assignPieces(characterId, pieceIds))
                .expectNextMatches(actualizado ->
                        actualizado.getId().equals(characterId) &&
                                (actualizado.getPieces() == null || actualizado.getPieces().isEmpty()))
                .verifyComplete();
    }

    @Test
    void assignPieces_errorAlBuscarPersonaje_propagaExcepcion() {
        String characterId = "char1";
        List<String> pieceIds = List.of("pieza1", "pieza2");

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.error(new RuntimeException("Error al buscar personaje")));

        StepVerifier.create(characterService.assignPieces(characterId, pieceIds))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error al buscar personaje"))
                .verify();
    }

    @Test
    void assignPieces_errorAlBuscarPiezas_propagaExcepcion() {
        String characterId = "char1";
        List<String> pieceIds = List.of("pieza1", "pieza2");

        Character personajeExistente = new Character();
        personajeExistente.setId(characterId);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(personajeExistente));

        when(pieceRepository.findByIdIn(pieceIds))
                .thenReturn(Flux.error(new RuntimeException("Error al buscar piezas")));

        StepVerifier.create(characterService.assignPieces(characterId, pieceIds))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error al buscar piezas"))
                .verify();
    }

    @Test
    void assignPieces_errorAlGuardarPersonaje_propagaExcepcion() {
        String characterId = "char1";
        List<String> pieceIds = List.of("pieza1", "pieza2");

        Character personajeExistente = new Character();
        personajeExistente.setId(characterId);

        Piece pieza1 = new Piece();
        pieza1.setId("pieza1");

        Piece pieza2 = new Piece();
        pieza2.setId("pieza2");

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(personajeExistente));

        when(pieceRepository.findByIdIn(pieceIds))
                .thenReturn(Flux.just(pieza1, pieza2));

        when(characterRepository.save(any(Character.class)))
                .thenReturn(Mono.error(new RuntimeException("Error al guardar personaje")));

        StepVerifier.create(characterService.assignPieces(characterId, pieceIds))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Error al guardar personaje"))
                .verify();
    }

    @Test
    void assignPiecesWithPowers_conCharacterIdNuloOVacio_lanzaIllegalArgumentException() {
        List<Piece> piezas = List.of(new Piece());

        assertThrows(IllegalArgumentException.class,
                () -> characterService.assignPiecesWithPowers(null, piezas).block());

        assertThrows(IllegalArgumentException.class,
                () -> characterService.assignPiecesWithPowers("", piezas).block());
    }

    @Test
    void assignPiecesWithPowers_conListaDePiezasNula_lanzaIllegalArgumentException() {
        String characterId = "char1";

        assertThrows(IllegalArgumentException.class,
                () -> characterService.assignPiecesWithPowers(characterId, null).block());
    }

    @Test
    void assignPiecesWithPowers_conListaDePiezasVacia_lanzaIllegalArgumentException() {
        String characterId = "char1";

        assertThrows(IllegalArgumentException.class,
                () -> characterService.assignPiecesWithPowers(characterId, List.of()).block());
    }

    @Test
    void assignPiecesWithPowers_personajeNoExiste_lanzaCharacterNotFoundException() {
        String characterId = "charInexistente";
        List<Piece> piezas = List.of(new Piece());

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.empty());

        StepVerifier.create(characterService.assignPiecesWithPowers(characterId, piezas))
                .expectErrorMatches(error ->
                        error instanceof CharacterNotFoundException &&
                                error.getMessage().contains("Personaje no encontrado"))
                .verify();
    }

    @Test
    void assignPiecesWithPowers_asignacionExitosa_retornaPersonajeActualizado() {
        String characterId = "char1";
        Piece pieza1 = new Piece();
        pieza1.setId("pieza1");

        List<Piece> piezas = List.of(pieza1);

        Character mockCharacter = new Character();
        mockCharacter.setId(characterId);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(mockCharacter));

        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        BuildService buildService = mock(BuildService.class);
        doNothing().when(buildService).clearPiecesCache(characterId);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.assignPiecesWithPowers(characterId, piezas))
                .expectNextMatches(updated ->
                        updated.getId().equals(characterId) &&
                                updated.getPieces().equals(piezas))
                .verifyComplete();
    }

    @Test
    void assignPiecesWithPowers_errorAlGuardarPersonaje_propagaExcepcion() {
        String characterId = "char1";
        Piece pieza1 = new Piece();
        pieza1.setId("pieza1");

        List<Piece> piezas = List.of(pieza1);

        Character mockCharacter = new Character();
        mockCharacter.setId(characterId);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(mockCharacter));

        when(characterRepository.save(any(Character.class)))
                .thenReturn(Mono.error(new RuntimeException("Error en MongoDB")));

        BuildService buildService = mock(BuildService.class);
        doNothing().when(buildService).clearPiecesCache(characterId);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.assignPiecesWithPowers(characterId, piezas))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().contains("Error en MongoDB"))
                .verify();
    }

    @Test
    void assignPiecesWithPowers_invocaClearPiecesCacheCorrectamente() {
        String characterId = "char1";
        Piece pieza1 = new Piece();
        pieza1.setId("pieza1");

        List<Piece> piezas = List.of(pieza1);

        Character mockCharacter = new Character();
        mockCharacter.setId(characterId);

        when(characterRepository.findById(characterId))
                .thenReturn(Mono.just(mockCharacter));

        when(characterRepository.save(any(Character.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        BuildService buildService = mock(BuildService.class);

        CharacterServiceImpl service = new CharacterServiceImpl(
                characterRepository,
                userRepository,
                buildRepository,
                buildService,
                pieceRepository,
                objectMapper
        );

        StepVerifier.create(service.assignPiecesWithPowers(characterId, piezas))
                .expectNextCount(1)
                .verifyComplete();

        verify(buildService).clearPiecesCache(characterId);
    }

}