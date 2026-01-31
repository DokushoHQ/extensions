package eu.kanade.tachiyomi.extension.all.dokusho

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class DokushoFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        DokushoAll(),
        DokushoEnglish(),
        DokushoJapanese(),
        DokushoJapaneseRomanized(),
        DokushoFrench(),
        DokushoKorean(),
        DokushoKoreanRomanized(),
        DokushoChineseHk(),
        DokushoChinese()
    )
}

class DokushoAll : Dokusho("all", "")
class DokushoEnglish : Dokusho("en", "En")
class DokushoJapanese : Dokusho("ja", "Jp")
class DokushoJapaneseRomanized : Dokusho("ja-ro", "JpRo")
class DokushoFrench : Dokusho("fr", "Fr")
class DokushoKorean : Dokusho("ko", "Ko")
class DokushoKoreanRomanized : Dokusho("ko-ro", "KoRo")
class DokushoChineseHk : Dokusho("zh-hk", "ZhHk")
class DokushoChinese : Dokusho("zh", "Zh")
