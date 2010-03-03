
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

import com.sun.javafx.tk.TKScene;
import com.sun.javafx.tk.swing.SwingScene;
import javafx.reflect.FXClassType;
import javafx.reflect.FXFunctionMember;
import javafx.reflect.FXLocal;
import javafx.reflect.FXLocal.ObjectValue;
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
 * Alternative: JXScene.java, also from jfxtras
 * Modified by daniel309: added method loadScene(Object fxscene, String classname)
 *
 * @example
 * <code>
 *  import org.jfxtras.scene.SceneToJComponent;
 *
 *  public class TheFrame extends javax.swing.JFrame {
 *
 *      public TheFrame() {
 *          getContentPane().add(SceneToJComponent.loadScene("scene.MyScene"), BorderLayout.CENTER);
 *      }
 *     public static void main(String args[]) {
 *         java.awt.EventQueue.invokeLater(new Runnable() {
 *             public void run() {
 *                 new TheFrame().setVisible(true);
 *             }
 *         });
 *     }
 *  }
 *  </code>
 *  @endexample
 *  <p>To run this, the jar files in the JavaFX SDK lib/shared and lib/desktop
 *  must be included in the java CLASSPATH.
 * @example
 * <code>
 * JAVAFX_HOME=/opt/javafx-1.2
 * SHARED=$JAVAFX_HOME/lib/shared
 * DESKTOP=$JAVAFX_HOME/lib/desktop

 * CP=dist/jfxtras.jar
 * CP=$CP:$SHARED/javafxrt.jar
 * CP=$CP:$SHARED/javafxc.jar
 * CP=$CP:$DESKTOP/decora-j2d-rsl.jar
 * CP=$CP:$DESKTOP/decora-ogl.jar
 * CP=$CP:$DESKTOP/decora-runtime.jar
 * CP=$CP:$DESKTOP/eula.jar
 * ...
 * java -cp $CP TheFrame
 * </code>
 * @endexample
 *
 * @profile Desktop
 * @author jclarke
 */
public class SceneToJComponent {
/*
    private static FXLocal.Context context = FXLocal.getContext();

    public static JComponent loadScene(String classname) {
        FXClassType classRef = context.findClass(classname);
        FXLocal.ObjectValue obj = (ObjectValue) classRef.newInstance();
        FXFunctionMember getPeer = classRef.getFunction("impl_getPeer");
        FXLocal.ObjectValue peer = (ObjectValue) getPeer.invoke(obj);
        SwingScene scene = (SwingScene)peer.asObject();

        return scene.scenePanel;
    }

    public static JComponent loadVSTPluginScene(String classname, VSTPluginAdapter plug) {
        FXClassType classRef = context.findClass(classname);
        FXLocal.ObjectValue obj = (ObjectValue) classRef.newInstance();

        //when not in DEBUG mode
        if (plug!=null) {
            //give the scene a reference to the plugin so that it can change
            //parameters when a slider is moved for instance
            FXGUIJavaInterop fxgui = (FXGUIJavaInterop)obj.asObject();
            fxgui.setPluginInstance(plug);

            //give the plugin a reference to the scene so that the gui
            //is updated when the plugin changes parameter values, e.g. when a
            //new program is loaded
            FXPluginJavaInterop fxplug = (FXPluginJavaInterop)plug;
            fxplug.setFXGUI(fxgui);
        }

        FXFunctionMember getPeer = classRef.getFunction("impl_getPeer");
        FXLocal.ObjectValue peer = (ObjectValue) getPeer.invoke(obj);
        SwingScene scene = (SwingScene)peer.asObject();

        return scene.scenePanel;
    }
*/
    public static JComponent loadVSTPluginScene2(String classname, VSTPluginAdapter plug) throws Exception {

				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				if (loader == null) {
					loader = SceneToJComponent.class.getClassLoader();
//					throw new Exception("Null class loader!");
				}
				
        Scene sc = (Scene)loader.loadClass(classname).newInstance();

/*
        //when not in DEBUG mode
        if (plug!=null) {
            //give the scene a reference to the plugin so that it can change
            //parameters when a slider is moved for instance
            FXGUIJavaInterop fxgui = (FXGUIJavaInterop)sc;
            fxgui.setPluginInstance(plug);

            //give the plugin a reference to the scene so that the gui
            //is updated when the plugin changes parameter values, e.g. when a
            //new program is loaded
            FXPluginJavaInterop fxplug = (FXPluginJavaInterop)plug;
            fxplug.setFXGUI(fxgui);
        }
*/      
        SwingScene scene = (SwingScene)sc.impl_getPeer();

        return scene.scenePanel;
    }
/*
    public static JComponent loadVSTPluginNode(String classname) {
        FXClassType classRef = context.findClass(classname);
        FXLocal.ObjectValue obj = (ObjectValue) classRef.newInstance();

        SceneFromNode sfn = (SceneFromNode)obj.asObject();
        
        javafx.scene.Scene sc = (Scene) sfn.getScene();
        SwingScene scene = (SwingScene) sc.impl_getPeer();

        return scene.scenePanel;
    }
 */   
}

