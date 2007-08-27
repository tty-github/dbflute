package org.seasar.dbflute.helper.language;

import org.seasar.dbflute.helper.language.grammar.DfGrammarInfo;
import org.seasar.dbflute.helper.language.grammar.DfGrammarInfoCSharp;
import org.seasar.dbflute.helper.language.metadata.LanguageMetaData;
import org.seasar.dbflute.helper.language.metadata.LanguageMetaDataCSharpOld;
import org.seasar.dbflute.helper.language.properties.DfDefaultDBFluteDicon;
import org.seasar.dbflute.helper.language.properties.DfDefaultDBFluteDiconCSharp;
import org.seasar.dbflute.helper.language.properties.DfGeneratedClassPackageDefault;
import org.seasar.dbflute.helper.language.properties.DfGeneratedClassPackageDefaultCSharpOld;

/**
 * The language depndency info of CSharpOld.
 * 
 * @author jflute
 */
public class DfLanguageDependencyInfoCSharpOld implements DfLanguageDependencyInfo {
    public DfGrammarInfo getGrammarInfo() {
        return new DfGrammarInfoCSharp();
    }

    public String getTemplateFileExtension() {
        return "vmnet";
    }

    public DfDefaultDBFluteDicon getDefaultDBFluteDicon() {
        return new DfDefaultDBFluteDiconCSharp();
    }

    public DfGeneratedClassPackageDefault getGeneratedClassPackageInfo() {
        return new DfGeneratedClassPackageDefaultCSharpOld();
    }

    public LanguageMetaData createLanguageMetaData() {
        return new LanguageMetaDataCSharpOld();
    }

    public String getDefaultSourceDirectory() {
        return "../source";
    }
}
