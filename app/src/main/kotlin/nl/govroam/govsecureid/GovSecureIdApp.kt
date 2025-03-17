package nl.govroam.govsecureid

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import org.tiqr.data.model.TiqrConfig
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class GovSecureIdApp : Application(), ImageLoaderFactory {

    @Inject
    internal lateinit var imageOkHttpClient: OkHttpClient.Builder

    override fun onCreate() {
        super.onCreate()

        TiqrConfig.initialize(this)
        // Setup Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Set the Coil's singleton instance
        Coil.setImageLoader(this)
    }

    /**
     * Use our own okHttp to share pools
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(context = this)
            .crossfade(enable = true)
            .okHttpClient {
                imageOkHttpClient
                    .build()
            }
            .build()
    }
}