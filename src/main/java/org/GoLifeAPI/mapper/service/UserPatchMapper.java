package org.GoLifeAPI.mapper.service;

import org.GoLifeAPI.dto.user.PatchUserDTO;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class UserPatchMapper {

    public Document mapPatchUserDtoToDoc(PatchUserDTO dto) {
        Document doc = new Document();
        String name = dto.getNombre();
        String surename = dto.getApellidos();
        if (name != null && !name.isBlank()) doc.append("nombre", name);
        if (surename != null && !surename.isBlank()) doc.append("apellidos", surename);
        return doc;
    }
}