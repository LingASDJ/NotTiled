package com.mirwanda.nottiled.langs;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mirwanda.nottiled.language;

import java.util.ArrayList;

// Method to initialize and populate language selection box and back button
public class SelectLangs {

    public static String[] getLangs(Skin skin, language z) {
        SelectBox sbLanguage = new SelectBox(skin);
        java.util.List<String> srr = new ArrayList<>();
        srr.add("English");
        srr.add("Chinese");
        sbLanguage.setItems(srr.toArray(new String[0]));
        TextButton bBack3 = new TextButton(z.back, skin);
        return new String[]{sbLanguage.getName(), bBack3.getName()};
    }
}
