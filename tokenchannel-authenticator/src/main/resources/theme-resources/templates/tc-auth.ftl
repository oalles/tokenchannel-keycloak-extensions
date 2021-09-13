<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
<#if section = "header">
${msg("tcTitle",realm.displayName)}
<#elseif section = "form">
<form id="kc-sms-code-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
	<div class="${properties.kcFormGroupClass!}">
		<div class="${properties.kcLabelWrapperClass!}">
			<label for="validation_code" class="${properties.kcLabelClass!}">${msg("tcLabel")}</label>
		</div>
		<div class="${properties.kcInputWrapperClass!}">
			<input type="text" id="validation_code" name="validation_code" class="${properties.kcInputClass!}" autofocus/>
		</div>
	</div>
	<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
		<div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
			<input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
		</div>
	</div>
</form>
<#elseif section = "info" >
${msg("tcDescription")}
</#if>
</@layout.registrationLayout>
