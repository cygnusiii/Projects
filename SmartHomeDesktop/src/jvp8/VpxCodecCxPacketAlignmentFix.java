package jvp8;

import java.util.*;

import org.bridj.*;

@SuppressWarnings("deprecation")
public final class VpxCodecCxPacketAlignmentFix extends StructCustomizer 
{
    @Override
    public void beforeLayout(StructDescription desc, List<StructFieldDescription> aggregatedFields)
    {
    	// Bridj doesn't detect the data field alignment properly
    	// on 32-bit Linux. It's solid other than this, so we just
    	// apply an override to change the alignment from 8 to 4.
    	StructFieldDescription dataField = aggregatedFields.get(1);
		if (Platform.isLinux() && !Platform.is64Bits())
		{
			dataField.alignment = 4;
		}
    }
}