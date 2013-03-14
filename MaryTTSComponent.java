

package com.tts;

import java.rmi.RemoteException;
import com.interfaces.SpeechProductionComponent;
import com.interfaces.VoiceProsodyComponent;

/**
 * This is the interface for the TTS server.
 * 
 * @author: Cody Canning; cody.canning@tufts.edu
 */ 
public interface MaryTTSComponent extends SpeechProductionComponent, VoiceProsodyComponent {

	public String getGuiHelp() throws RemoteException;

}
