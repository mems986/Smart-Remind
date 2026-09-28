package com.smartremind.app.util

import android.content.Context
import android.content.res.Configuration
import com.smartremind.app.data.LANG_AUTO
import java.util.Locale

object LocaleHelper {
    /** Повертає Context з потрібною мовою — для тексту у сповіщенні, що будується поза Activity. */
    fun wrap(context: Context, language: String): Context {
        if (language == LANG_AUTO) return context
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(language))
        return context.createConfigurationContext(config)
    }
}
