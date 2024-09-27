package com.hmdp.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.Result;
import com.hmdp.utils.AliOssUtil;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 文件上传
     * @param image
     * @return
     */
    @PostMapping("blog")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        log.info("文件上传：{}",image);
        try {
            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();
            //截取原始文件名的后缀
            String extension=originalFilename.substring(originalFilename.lastIndexOf("."));
            // 生成新文件名
            String fileName = UUID.randomUUID().toString()+extension;
            //文件请求路径
            String filePath=aliOssUtil.upload(image.getBytes(),fileName);
            return Result.ok(filePath);
        } catch (IOException e) {
           log.info("文件上传失败：{}",e);
        }
        return Result.fail("文件上传失败！");
    }

    @GetMapping("/blog/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        File file = new File(String.valueOf(SystemConstants.IMAGE_UPLOAD_DIR), filename);
        if (file.isDirectory()) {
            return Result.fail("错误的文件名称");
        }
        FileUtil.del(file);
        return Result.ok();
    }

    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // 判断目录是否存在
        File dir = new File(String.valueOf(SystemConstants.IMAGE_UPLOAD_DIR), StrUtil.format("/blogs/{}/{}", d1, d2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }
}
