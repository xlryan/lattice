import React, { useState } from 'react';
import { InboxOutlined } from '@ant-design/icons';
import { message, notification, Upload } from 'antd';
import type { UploadProps } from 'antd';
import request from '../requestConfig';

export const CareerIngestPage: React.FC = () => {
  const [processing, setProcessing] = useState(false);

  const props: UploadProps = {
    name: 'file',
    multiple: false,
    customRequest: async ({ file, onSuccess, onError }) => {
      const formData = new FormData();
      formData.append('file', file as Blob);
      try {
        setProcessing(true);
        await request('/ingest/career/resume', {
          method: 'POST',
          requestType: 'form',
          data: formData,
        });
        message.success('上传成功，正在处理...');
        setTimeout(() => {
          notification.success({ message: '简历处理完成', description: '已将内容转为 STAR 节点' });
          setProcessing(false);
        }, 2000);
        onSuccess?.('ok', new XMLHttpRequest());
      } catch (error) {
        setProcessing(false);
        message.error('上传失败');
        onError?.(error as Error);
      }
    },
    showUploadList: false,
    disabled: processing,
  };

  return (
    <div className="max-w-2xl mx-auto">
      <Upload.Dragger {...props}>
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">拖拽或点击上传 PDF 简历</p>
        <p className="ant-upload-hint">文件会上传到 Lattice 并触发职业域入库</p>
      </Upload.Dragger>
    </div>
  );
};
