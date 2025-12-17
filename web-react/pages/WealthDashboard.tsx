import React, { useEffect, useMemo, useState } from 'react';
import { Card, Col, Row, Spin, Typography } from 'antd';
import ReactECharts from 'echarts-for-react';
import { fetchAssetAllocation, fetchMonthlyExpenses } from '../services/lattice/wealth';
import type { AssetAllocationItem, MonthlyExpenseItem } from '../services/lattice/types';

export const WealthDashboard: React.FC = () => {
  const [allocation, setAllocation] = useState<AssetAllocationItem[]>([]);
  const [expenses, setExpenses] = useState<MonthlyExpenseItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [alloc, exp] = await Promise.all([
          fetchAssetAllocation(),
          fetchMonthlyExpenses()
        ]);
        setAllocation(alloc);
        setExpenses(exp);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const pieOption = useMemo(() => ({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: '70%',
        data: allocation.map((item) => ({ value: item.value, name: item.label })),
        label: {
          formatter: '{b}: {d}%'
        }
      }
    ]
  }), [allocation]);

  const lineOption = useMemo(() => ({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: expenses.map((item) => item.month)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        type: 'line',
        smooth: true,
        data: expenses.map((item) => item.amount)
      }
    ]
  }), [expenses]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <Row gutter={24}>
      <Col span={12}>
        <Card title="资产配置" bordered={false}>
          <ReactECharts option={pieOption} notMerge lazyUpdate style={{ height: 360 }} />
        </Card>
      </Col>
      <Col span={12}>
        <Card title="近 6 个月支出趋势" bordered={false}>
          <ReactECharts option={lineOption} notMerge lazyUpdate style={{ height: 360 }} />
        </Card>
      </Col>
      <Col span={24} className="mt-6">
        <Card>
          <Typography.Paragraph>
            最新更新时间：{new Date().toLocaleString()}，当前跟踪账户数：{allocation.length}
          </Typography.Paragraph>
        </Card>
      </Col>
    </Row>
  );
};
