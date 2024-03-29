package vpx;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * <i>native declaration : vpx\vp8.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("vpx") 
public class vp8_postproc_cfg_t extends StructObject {
	public vp8_postproc_cfg_t() {
		super();
	}
	/// < the types of post processing to be done, should be combination of "vp8_postproc_level"
	@Field(0) 
	public int post_proc_flag() {
		return this.io.getIntField(this, 0);
	}
	/// < the types of post processing to be done, should be combination of "vp8_postproc_level"
	@Field(0) 
	public vp8_postproc_cfg_t post_proc_flag(int post_proc_flag) {
		this.io.setIntField(this, 0, post_proc_flag);
		return this;
	}
	/// < the strength of deblocking, valid range [0, 16]
	@Field(1) 
	public int deblocking_level() {
		return this.io.getIntField(this, 1);
	}
	/// < the strength of deblocking, valid range [0, 16]
	@Field(1) 
	public vp8_postproc_cfg_t deblocking_level(int deblocking_level) {
		this.io.setIntField(this, 1, deblocking_level);
		return this;
	}
	/// < the strength of additive noise, valid range [0, 16]
	@Field(2) 
	public int noise_level() {
		return this.io.getIntField(this, 2);
	}
	/// < the strength of additive noise, valid range [0, 16]
	@Field(2) 
	public vp8_postproc_cfg_t noise_level(int noise_level) {
		this.io.setIntField(this, 2, noise_level);
		return this;
	}
	public vp8_postproc_cfg_t(Pointer pointer) {
		super(pointer);
	}
}
