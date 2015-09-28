﻿/*
 Copyright (c) 2003-2014, CKSource - Frederico Knabben. All rights reserved.
 For licensing, see LICENSE.md or http://ckeditor.com/license
*/
CKEDITOR.addTemplates("default", {
  imagesPath: CKEDITOR.getUrl(CKEDITOR.plugins.getPath("templates") + "templates/images/"),
  templates: [
    {
      title: "Image and Title",
      image: "template1.gif",
      description: "One main image with a title and text that surround the image.",
      html: '<h3><img src=" " alt="" style="margin-right: 10px" height="100" width="100" align="left" />Type the title here</h3><p>Type the text here</p>'
    },
    {
      title: "Two-Column Layout",
      image: "template2.gif",
      description: "A template that defines two columns, each with some text.",
      html: '<table role="presentation" cellspacing="0" cellpadding="0" style="width:100%" border="0"><tr><td style="width:49.5%;vertical-align:top;">Text 1</td><td style="width:1%">&nbsp;</td><td style="width:49.5%;vertical-align:top;">Text 2</td></tr></table><p>More text goes here.</p>'
    },
    {
      title: "Text and Table",
      image: "template3.gif",
      description: "A title with some text and a table.",
      html: '<div style="width: 80%"><h3>Title goes here</h3><table style="width:150px;float: right" cellspacing="0" cellpadding="0" border="1"><caption style="border:solid 1px black"><strong>Table title</strong></caption><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr></table><p>Type the text here</p></div>'
    }
  ]
});
