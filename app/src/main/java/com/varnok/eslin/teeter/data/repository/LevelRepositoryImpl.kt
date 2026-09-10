package com.varnok.eslin.teeter.data.repository

import com.varnok.eslin.teeter.data.catalog.LevelCatalog
import com.varnok.eslin.teeter.domain.model.Award
import com.varnok.eslin.teeter.domain.model.Chapter
import com.varnok.eslin.teeter.domain.model.LevelSpec
import com.varnok.eslin.teeter.domain.repository.LevelRepository

class LevelRepositoryImpl : LevelRepository {
    override fun levels(): List<LevelSpec> = LevelCatalog.levels
    override fun chapters(): List<Chapter> = LevelCatalog.chapters
    override fun awards(): List<Award> = LevelCatalog.awards
    override fun endless(): LevelSpec = LevelCatalog.endlessSpec()
    override fun level(index: Int): LevelSpec = LevelCatalog.levels[index.coerceIn(0, LevelCatalog.levels.lastIndex)]
}
