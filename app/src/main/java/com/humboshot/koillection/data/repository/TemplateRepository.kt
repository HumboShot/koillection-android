package com.humboshot.koillection.data.repository

import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.TemplateService
import com.humboshot.koillection.data.template.Field
import com.humboshot.koillection.data.template.FieldWrite
import com.humboshot.koillection.data.template.Template
import com.humboshot.koillection.data.template.TemplateWrite

object TemplateRepository {
    private val service: TemplateService
        get() = ApiClient.getInstance().create(TemplateService::class.java)

    suspend fun getTemplates(): Result<List<Template>> =
        getAllPages { page -> service.getTemplates(page) }
            .map { templates -> templates.sortedBy { it.name.lowercase() } }

    suspend fun getTemplate(id: String): Result<Template> = service.getTemplate(id)

    suspend fun getFields(templateId: String): Result<List<Field>> =
        getAllPages { page -> service.getFields(templateId, page) }
            .map { fields -> fields.sortedBy { it.position } }

    suspend fun createTemplate(name: String): Result<Template> = service.createTemplate(TemplateWrite(name = name))

    suspend fun renameTemplate(id: String, name: String): Result<Template> =
        service.updateTemplate(id, TemplateWrite(name = name))

    suspend fun deleteTemplate(id: String): Result<Unit> = service.deleteTemplate(id)

    suspend fun createField(field: FieldWrite): Result<Field> = service.createField(field)

    suspend fun updateField(id: String, field: FieldWrite): Result<Field> = service.updateField(id, field)

    suspend fun deleteField(id: String): Result<Unit> = service.deleteField(id)
}
