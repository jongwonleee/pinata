package capstone.aiimageeditor.ui

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import capstone.aiimageeditor.R
import com.yydcdut.markdown.MarkdownConfiguration
import com.yydcdut.markdown.MarkdownProcessor
import com.yydcdut.markdown.syntax.text.TextFactory
import com.yydcdut.markdown.theme.ThemeSunburst
import kotlinx.android.synthetic.main.activity_copyright.*
import kotlinx.android.synthetic.main.activity_setting.*


class CopyrightActivity : Activity() {
    companion object{
        val str="""
## <Copyrights 2020. @jongwonleee. All rights reserved.>
### _contributed with. @samsohn0168, @hyemin-Jeong_
```markdown

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

```
mm
----
##__opensource license__
  
### [intel]
#### [https://opencv.org/license/]
```markdown
  
Copyright (C) 2000-2019, Intel Corporation, all rights reserved.
Copyright (C) 2009-2011, Willow Garage Inc., all rights reserved.
Copyright (C) 2009-2016, NVIDIA Corporation, all rights reserved.
Copyright (C) 2010-2013, Advanced Micro Devices, Inc., all rights reserved.
Copyright (C) 2015-2016, OpenCV Foundation, all rights reserved.
Copyright (C) 2015-2016, Itseez Inc., all rights reserved.
Copyright (C) 2019-2020, Xperience AI, all rights reserved.

Third party copyrights are property of their respective owners.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the names of the copyright holders nor the names of the contributors may be used to endorse or promote products derived from this software without specific prior written permission.
This software is provided by the copyright holders and contributors “as is” and any express or implied warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall copyright holders or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
  
```
  
### [tensorflow]
#### [https://github.com/tensorflow]
#### [license][https://github.com/tensorflow/tensorflow/blob/master/LICENSE]
  

### [bumptech]
#### [https://github.com/bumptech/glide]
```markdown
  
Copyright 2016 yydcdut
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
  
```
  

### [cats-oss]
#### [https://github.com/cats-oss/android-gpuimage]
```markdown
  
Copyright 2018 CyberAgent, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License
  
```
  

### gun0912
#### [https://github.com/ParkSangGwon/TedPermission]
```markdown
  
Copyright 2017 Ted Park
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
  
```
  
### yydcdut
#### [https://github.com/yydcdut/RxMarkdown]
```markdown
  
Copyright 2016 yydcdut
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
  
```
  

### yukuku
#### [https://github.com/yukuku/ambilwarna]
  

### [elye]
#### [https://github.com/elye/demo_android_draw_bitmap_mesh]
  

### [flaticon]
#### [https://www.flaticon.com/authors/freepik]
```
Icons made by flaticon
```
  
"""
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_copyright)
       /* val markdownProcessor = MarkdownProcessor(this)
        markdownProcessor.factory(TextFactory.create())
        val markdownConfiguration = MarkdownConfiguration()
        markdownProcessor.config(markdownConfiguration)
        textCopyright.text=markdownProcessor.parse("##text")*/
        val markdownConfiguration = MarkdownConfiguration.Builder(this)
            .setDefaultImageSize(50, 50)
            .setBlockQuotesLineColor(-0xcc4a1b)
            .setHeader1RelativeSize(1.3f)
            .setHeader2RelativeSize(1.2f)
            .setHeader3RelativeSize(1.1f)
            .setHeader4RelativeSize(1.0f)
            .setHeader5RelativeSize(1.0f)
            .setHeader6RelativeSize(1.0f)
            .setHorizontalRulesColor(-0x663400)
            .setCodeBgColor(-0xFFF)
            .setTodoColor(-0x559934)
            .setTodoDoneColor(-0x7800)
            .setUnOrderListColor(-0xff2201)
            .setHorizontalRulesHeight(1)
            .setLinkFontColor(Color.BLUE)
            .showLinkUnderline(false)
            .setTheme(ThemeSunburst())
            .setOnLinkClickCallback { view, link -> toast(link) }
            .setOnTodoClickCallback { view, line, lineNumber ->
                toast("line:$line\nline number:$lineNumber")
                textCopyright.text
            }
            .build()
        val processor = MarkdownProcessor(this)
        processor.factory(TextFactory.create())
        processor.config(markdownConfiguration)
        textCopyright.text = processor.parse(str)
    }

    private fun toast(msg: String) {
        var mToast:Toast? =null
        if (mToast == null) {
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        }
        mToast?.setText(msg)
        mToast?.show()
    }
}