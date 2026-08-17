package com.yunkesoftware.www.poster;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PosterTemplateValidatorTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void acceptsOnlyTheDocumentedLayerTypesAndFields() throws Exception {
        String json = """
                {
                  "canvas":{"width":750,"height":1000},
                  "declaredCustomFields":["headline"],
                  "layers":[
                    {"type":"staticText","x":20,"y":20,"width":700,"height":60,"text":"金融知识","fontSize":32,"color":"#222222"},
                    {"type":"editableText","x":20,"y":100,"width":700,"height":60,"field":"custom.headline","fontSize":28},
                    {"type":"qrCode","x":600,"y":880,"width":120,"height":120,"field":"generatedAt"}
                  ]
                }
                """;
        assertDoesNotThrow(() -> PosterTemplateValidator.validate(mapper.readTree(json)));
    }

    @Test
    void rejectsExecutableFieldsAndExternalTemplateExpressions() throws Exception {
        String json = """
                {
                  "canvas":{"width":750,"height":1000},
                  "declaredCustomFields":[],
                  "layers":[{"type":"staticText","x":0,"y":0,"width":750,"height":60,"text":"${javascript:alert(1)}","expression":"alert(1)"}]
                }
                """;
        assertThrows(IllegalArgumentException.class, () -> PosterTemplateValidator.validate(mapper.readTree(json)));
    }

    @Test
    void rejectsOutOfBoundsLayersAndUndeclaredCustomFields() throws Exception {
        String json = """
                {
                  "canvas":{"width":750,"height":1000},
                  "declaredCustomFields":[],
                  "layers":[{"type":"dynamicText","x":700,"y":0,"width":100,"height":60,"field":"custom.notDeclared"}]
                }
                """;
        assertThrows(IllegalArgumentException.class, () -> PosterTemplateValidator.validate(mapper.readTree(json)));
    }

    @Test
    void rejectsDuplicateStaticAssetKeys() throws Exception {
        String json = """
                {
                  "canvas":{"width":750,"height":1000},
                  "declaredCustomFields":[],
                  "layers":[
                    {"type":"staticImage","x":0,"y":0,"width":100,"height":100,"assetKey":"logo"},
                    {"type":"staticImage","x":120,"y":0,"width":100,"height":100,"assetKey":"logo"}
                  ]
                }
                """;
        assertThrows(IllegalArgumentException.class, () -> PosterTemplateValidator.validate(mapper.readTree(json)));
    }
}
