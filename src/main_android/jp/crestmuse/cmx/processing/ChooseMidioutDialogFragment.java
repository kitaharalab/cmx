package jp.crestmuse.cmx.processing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;

import java.util.List;

import jp.crestmuse.cmx.sound.SoundUtils;
import jp.kshoji.javax.sound.midi.MidiDevice;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

public class ChooseMidioutDialogFragment extends DialogFragment {
    CMXController cmx;

    public ChooseMidioutDialogFragment setCMXController(CMXController cmx) {
        this.cmx = cmx;
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            final List<MidiDevice.Info> midiOutDeviceInfo = SoundUtils.getMidiOutDeviceInfo();
            // Set the dialog title
            builder.setTitle("Select MIDI OUT Device...")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setSingleChoiceItems(
                            new ArrayAdapter(getActivity() , 0, midiOutDeviceInfo),
                            0,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    cmx.setMidiOutDevice(midiOutDeviceInfo.get(which).getName());
                                    dialog.dismiss();
                                }
                            });
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }

        return builder.create();
    }
}
