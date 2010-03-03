/*
 * Copyright (c) 2008-2009, JFXtras Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of JFXtras nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import com.sun.javafx.tk.swing.SwingScene;
import javafx.scene.Scene;
import javax.swing.JComponent;
import jvst.wrapper.VSTPluginAdapter;

/**
 * A Java method that allows a JavaFX Scene to be displayed in a Swing application.
 * from 
 * http://code.google.com/p/jfxtras/source/browse/jfxtras.core/trunk/src/org/jfxtras/scene/SceneToJComponent.java
 *
 * NOTE: works with JavaFX 1.2
 *
 * Original author: jclarke ?
 *
 * Alternative: JXScene.java, also from jfxtras
 * Modified by daniel309: added method loadScene(Object fxscene, String classname)
 * Modified by thbar: keep only what is useful for Opaz
 */
public class SceneToJComponent {
    public static JComponent loadScene(String classname) throws Exception {
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				if (loader == null) {
					// work around the Mac OS weirdness - can be null (JNI initialization ?)
					loader = SceneToJComponent.class.getClassLoader();
				}
        Scene sc = (Scene)loader.loadClass(classname).newInstance();
        SwingScene scene = (SwingScene)sc.impl_getPeer();
        return scene.scenePanel;
    }
}

