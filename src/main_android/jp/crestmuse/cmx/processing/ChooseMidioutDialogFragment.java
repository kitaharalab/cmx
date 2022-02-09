package jp.crestmuse.cmx.processing;

//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
//import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import androidx.fragment.app.DialogFragment;

import java.util.List;

import jp.crestmuse.cmx.sound.SoundUtils;
import jp.kshoji.javax.sound.midi.MidiDevice;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

public class ChooseMidioutDialogFragment extends DialogFragment {
    protected CMXController cmx;
    protected int layout = 0;

    public ChooseMidioutDialogFragment setCMXController(CMXController cmx) {
        this.cmx = cmx;
        return this;
    }

    public ChooseMidioutDialogFragment setLayout(int layout) {
        this.layout = layout;
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            final List<MidiDevice.Info> midiOutDeviceInfo = SoundUtils.getMidiOutDeviceInfo();
            builder.setTitle("Select MIDI OUT Device...")
                .setSingleChoiceItems(
                    new ArrayAdapter(getActivity() , layout, midiOutDeviceInfo),
                    0,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cmx.setMidiOutDevice(midiOutDeviceInfo.get(which).getName());
                            dialog.dismiss();
                        }
                    });
        } catch (MidiUnavailableException e) {
			throw new DeviceNotAvailableException("MIDI device not available");
        }
        return builder.create();
    }
}
