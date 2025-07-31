package org.GoLifeAPI.mapper.service;

import org.GoLifeAPI.dto.goal.PatchBoolGoalDTO;
import org.GoLifeAPI.dto.goal.PatchGoalDTO;
import org.GoLifeAPI.dto.goal.PatchNumGoalDTO;
import org.GoLifeAPI.model.Enums;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class GoalPatchMapper {

    public Document mapFinalizePatchToDoc() {
        return new Document("finalizado", true);
    }

    public Document mapPatchBoolGoalDtoToDoc(PatchBoolGoalDTO dto) {
        Document doc = new Document();
        mapPatchGoalDtoToDoc(dto, doc);
        return doc;
    }

    public Document mapPatchNumGoalDtoToDoc(PatchNumGoalDTO dto) {
        Document doc = new Document();
        Double goalValue = dto.getValorObjetivo();
        String unit = dto.getUnidad();
        mapPatchGoalDtoToDoc(dto, doc);
        if (goalValue != null) doc.append("valorObjetivo", goalValue);
        if (unit != null && !unit.isBlank()) doc.append("unidad", unit);
        return doc;
    }

    public Document mapPatchGoalDtoToPartialDoc(PatchGoalDTO dto) {
        Document doc = new Document();
        String name = dto.getNombre();
        Enums.Duracion durationUnit = dto.getDuracionUnidad();
        if (name != null && !name.isBlank()) doc.append("nombre", name);
        appendCorrectDuracionValor(doc, dto);
        if (durationUnit != null) doc.append("duracionUnidad", durationUnit);
        return doc;
    }

    private void mapPatchGoalDtoToDoc(PatchGoalDTO dto, Document doc) {
        String name = dto.getNombre();
        String desc = dto.getDescripcion();
        Enums.Duracion durationUnit = dto.getDuracionUnidad();
        if (name != null && !name.isBlank()) doc.append("nombre", name);
        if (desc != null) doc.append("descripcion", desc);
        appendCorrectDuracionValor(doc, dto);
        if (durationUnit != null) doc.append("duracionUnidad", durationUnit);
    }

    private void appendCorrectDuracionValor(Document doc, PatchGoalDTO dto) {
        Integer durationValue = dto.getDuracionValor();
        Enums.Duracion durationUnit = dto.getDuracionUnidad();
        if (durationUnit == null) {
            if (durationValue != null && durationValue > 0)
                doc.append("duracionValor", durationValue);
        } else if (durationUnit.toString().equalsIgnoreCase("Indefinido"))
            doc.append("duracionValor", -1);
        else {
            if (durationValue != null && durationValue > 0)
                doc.append("duracionValor", durationValue);
            else
                doc.append("duracionValor", 1);
        }
    }
}