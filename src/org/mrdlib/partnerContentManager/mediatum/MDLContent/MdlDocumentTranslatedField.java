package org.mrdlib.partnerContentManager.mediatum.MDLContent;

public class MdlDocumentTranslatedField {

	long document_id;
	MdlDocumentTranslatedFieldFieldType field_type;
	MdlDocumentTranslatedFieldTranslationTool translation_tool;
	String source_language;
	String target_language;
	String text;
	
	public MdlDocumentTranslatedField() {
		super();
	}
	
	public MdlDocumentTranslatedField(long document_id, MdlDocumentTranslatedFieldFieldType field_type,
			MdlDocumentTranslatedFieldTranslationTool translation_tool, String source_language, String target_language,
			String text) {
		super();
		this.document_id = document_id;
		this.field_type = field_type;
		this.translation_tool = translation_tool;
		this.source_language = source_language;
		this.target_language = target_language;
		this.text = text;
	}
	
	public long getDocument_id() {
		return document_id;
	}
	
	public void setDocument_id(long document_id) {
		this.document_id = document_id;
	}
	
	public MdlDocumentTranslatedFieldFieldType getField_type() {
		return field_type;
	}
	
	public void setField_type(MdlDocumentTranslatedFieldFieldType field_type) {
		this.field_type = field_type;
	}
	
	public MdlDocumentTranslatedFieldTranslationTool getTranslation_tool() {
		return translation_tool;
	}
	
	public void setTranslation_tool(MdlDocumentTranslatedFieldTranslationTool translation_tool) {
		this.translation_tool = translation_tool;
	}
	
	public String getSource_language() {
		return source_language;
	}
	
	public void setSource_language(String source_language) {
		this.source_language = source_language;
	}
	
	public String getTarget_language() {
		return target_language;
	}
	
	public void setTarget_language(String target_language) {
		this.target_language = target_language;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
}
