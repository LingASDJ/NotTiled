package com.mirwanda.nottiled.utils;

import com.badlogic.gdx.Gdx;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.CrtMonitor;
import com.bitfire.postprocessing.effects.Curvature;
import com.bitfire.postprocessing.effects.Vignette;
import com.bitfire.postprocessing.effects.Zoomer;
import com.bitfire.postprocessing.filters.Combine;
import com.bitfire.postprocessing.filters.CrtScreen;
import com.bitfire.postprocessing.filters.RadialBlur;
import com.bitfire.utils.ShaderLoader;
import com.mirwanda.nottiled.NotiledAndoridPro;

public class NotTiledUntils {
    public static final boolean isDesktop = false;
    /** NTCN
     * 后置处理器初始化
     * @param game 主程序接口
     */
    public static void initializePostProcessor(NotiledAndoridPro game) { //I DONT EVEN KNOW WHAT THESE MEANS LOL
        ShaderLoader.BasePath = "data/shaders/";
        game.postProcessor = new PostProcessor( false, true, isDesktop );
        game.postProcessor.setClearColor( .5f, .5f, .5f, 1 );
        int vpW = Gdx.graphics.getWidth();
        int vpH = Gdx.graphics.getHeight();
        // create the effects you want
        game.bloom = new Bloom( (int) (Gdx.graphics.getWidth() * 0.25f), (int) (Gdx.graphics.getHeight() * 0.25f) );
        game.curvature = new Curvature();
        game.zoomer = new Zoomer( vpW, vpH, isDesktop ? RadialBlur.Quality.VeryHigh : RadialBlur.Quality.Low );
        int effects = CrtScreen.Effect.TweakContrast.v | CrtScreen.Effect.PhosphorVibrance.v | CrtScreen.Effect.Scanlines.v | CrtScreen.Effect.Tint.v;
        game.crt = new CrtMonitor( vpW, vpH, false, false, CrtScreen.RgbMode.ChromaticAberrations, effects );
        game.crt.setTint( 0.8f, 0.8f, .8f );
        Combine combine = game.crt.getCombinePass();
        combine.setSource1Intensity( 0f );
        combine.setSource2Intensity( 1f );
        combine.setSource1Saturation( 0f );
        combine.setSource2Saturation( 1f );
        game.vignette = new Vignette( vpW, vpH, false );

        game.postProcessor.addEffect( game.curvature );
        game.postProcessor.addEffect( game.zoomer );
        game.postProcessor.addEffect( game.vignette );
        game.postProcessor.addEffect( game.crt );
        game.postProcessor.addEffect( game.bloom );
        game.bloom.setBaseIntesity( 0.0f );
        game.bloom.setBlurAmount( 1 );//5
        game.zoomer.setBlurStrength( -0.1f );
        game.zoomer.setOrigin( Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f );
        game.curvature.setZoom( 1f );
        game.vignette.setIntensity( 1f );
        game.bloom.setEnabled( false );
        game.crt.setEnabled( false );
        game.vignette.setEnabled( false );
        game.curvature.setEnabled( false );
        game.zoomer.setEnabled( false );
    }
}
