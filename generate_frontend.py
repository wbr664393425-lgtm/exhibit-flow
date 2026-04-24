#!/usr/bin/env python3
"""Generate PC frontend API + Vue pages for eh_* modules."""
import os

BASE = '/Users/wbr/Documents/project/exhibit-flow/exhibit-flow-vue/src'

# (module_name, cn_name, api_url_prefix, perm_prefix, query_fields, table_columns, form_fields)
MODULES = [
    {
        'name': 'apply',
        'cn': '展厅申请',
        'url': '/admin/eh/apply',
        'perm': 'eh_apply',
        'query': [
            ('subject', '会议主题', 'input'),
            ('status', '申请状态', 'select', [('0','草稿'),('1','待审批'),('2','已通过'),('3','已驳回'),('4','已取消'),('5','已改期')]),
            ('createTime', '创建时间', 'daterange'),
        ],
        'columns': [
            ('subject', '会议主题'),
            ('visitorCompany', '来访单位'),
            ('visitorCount', '人数', 'width="80"'),
            ('startTime', '开始时段'),
            ('endTime', '结束时段'),
            ('applicant', '申请人'),
            ('status', '状态'),
            ('createTime', '创建时间', 'sortable="custom" width="180"'),
        ],
        'form': [
            ('subject', '会议主题', 'input', True),
            ('agenda', '简要议程', 'textarea', False),
            ('startTime', '开始时段', 'datetime', True),
            ('endTime', '结束时段', 'datetime', True),
            ('visitorCompany', '来访单位', 'input', True),
            ('industry', '所属行业', 'input', False),
            ('visitorCount', '人数', 'number', False),
            ('topLeaderTitle', '最高陪同领导级别', 'input', False),
            ('applicant', '申请人', 'input', True),
            ('applicantDept', '申请部门', 'input', False),
            ('phone', '联系电话', 'input', False),
            ('district', '所属区县', 'input', False),
            ('extraServices', '附加服务', 'input', False),
            ('remark', '备注', 'textarea', False),
        ],
    },
    {
        'name': 'approval',
        'cn': '审批节点',
        'url': '/admin/eh/approval',
        'perm': 'eh_approval_node',
        'query': [
            ('applyId', '申请ID', 'input'),
        ],
        'columns': [
            ('applyId', '申请ID'),
            ('nodeLevel', '审批层级', 'width="100"'),
            ('approver', '审批人'),
            ('action', '动作'),
            ('opinion', '审批意见'),
            ('status', '节点状态'),
            ('actionTime', '操作时间', 'width="180"'),
        ],
        'form': [
            ('applyId', '关联申请ID', 'input', True),
            ('nodeLevel', '审批层级', 'number', True),
            ('approver', '审批人', 'input', True),
            ('action', '动作(agree/reject/transfer)', 'input', True),
            ('opinion', '审批意见', 'textarea', False),
        ],
    },
    {
        'name': 'changelog',
        'cn': '改期取消日志',
        'url': '/admin/eh/changelog',
        'perm': 'eh_change_log',
        'query': [
            ('applyId', '申请ID', 'input'),
            ('changeType', '变更类型', 'select', [('cancel','取消'),('reschedule','改期'),('supplement','补录')]),
        ],
        'columns': [
            ('applyId', '申请ID'),
            ('changeType', '变更类型', 'width="120"'),
            ('oldStartTime', '原开始时段', 'width="180"'),
            ('newStartTime', '新开始时段', 'width="180"'),
            ('reason', '变更原因'),
            ('createBy', '操作人'),
            ('createTime', '操作时间', 'width="180"'),
        ],
        'form': [
            ('applyId', '关联申请ID', 'input', True),
            ('changeType', '变更类型', 'input', True),
            ('reason', '变更原因', 'textarea', True),
        ],
    },
    {
        'name': 'visit',
        'cn': '参观留存',
        'url': '/admin/eh/visit',
        'perm': 'eh_visit_record',
        'query': [
            ('applyId', '申请ID', 'input'),
            ('status', '留存状态', 'select', [('0','待录入'),('1','已录入'),('2','已归还')]),
        ],
        'columns': [
            ('applyId', '申请ID'),
            ('actualVisitTime', '实际参观时间', 'width="180"'),
            ('actualVisitCount', '实际人数', 'width="100"'),
            ('ourLeaderLevel', '我方领导级别'),
            ('opportunityCode', '商机编码'),
            ('returnSigner', '归还签字人'),
            ('status', '状态', 'width="100"'),
            ('createTime', '创建时间', 'width="180"'),
        ],
        'form': [
            ('applyId', '关联申请ID', 'input', True),
            ('actualVisitTime', '实际参观时间', 'datetime', True),
            ('actualVisitCount', '实际参观人数', 'number', True),
            ('ourLeaderLevel', '我方领导参会最高级别', 'input', False),
            ('opportunityCode', '商机编码', 'input', False),
            ('returnSigner', '归还签字人', 'input', False),
            ('status', '留存状态', 'input', False),
        ],
    },
    {
        'name': 'visit-photo',
        'cn': '现场照片',
        'url': '/admin/eh/visit/photo',
        'perm': 'eh_visit_photo',
        'query': [
            ('visitId', '留存ID', 'input'),
        ],
        'columns': [
            ('visitId', '留存ID'),
            ('fileName', '文件名'),
            ('fileSize', '文件大小', 'width="120"'),
            ('createBy', '上传人'),
            ('createTime', '上传时间', 'width="180"'),
        ],
        'form': [
            ('visitId', '关联留存ID', 'input', True),
            ('fileUrl', '图片URL', 'input', True),
            ('fileName', '文件名', 'input', False),
        ],
    },
    {
        'name': 'opportunity',
        'cn': '商机跟进',
        'url': '/admin/eh/opportunity',
        'perm': 'eh_opportunity',
        'query': [
            ('opportunityCode', '商机编码', 'input'),
            ('status', '商机状态', 'select', [('clue','线索'),('opportunity','商机'),('signed','签约')]),
        ],
        'columns': [
            ('visitId', '留存ID'),
            ('applyId', '申请ID'),
            ('opportunityCode', '商机编码'),
            ('status', '商机状态', 'width="120"'),
            ('remark', '备注'),
            ('createBy', '创建人'),
            ('createTime', '创建时间', 'width="180"'),
        ],
        'form': [
            ('visitId', '关联留存ID', 'input', True),
            ('applyId', '关联申请ID', 'input', False),
            ('opportunityCode', '商机编码', 'input', True),
            ('status', '商机状态', 'input', True),
            ('remark', '备注', 'textarea', False),
        ],
    },
]


def gen_api(mod):
    name = mod['name']
    url = mod['url']
    camel = name.replace('-', '')
    return f"""import request from '/@/utils/request';

export const fetchList = (params?: Object) => {{
\treturn request({{
\t\turl: '{url}/page',
\t\tmethod: 'get',
\t\tparams,
\t}});
}};

export const getObj = (id: string) => {{
\treturn request({{
\t\turl: '{url}/details/' + id,
\t\tmethod: 'get',
\t}});
}};

export const addObj = (obj: Object) => {{
\treturn request({{
\t\turl: '{url}',
\t\tmethod: 'post',
\t\tdata: obj,
\t}});
}};

export const putObj = (obj: Object) => {{
\treturn request({{
\t\turl: '{url}',
\t\tmethod: 'put',
\t\tdata: obj,
\t}});
}};

export const delObj = (ids: object) => {{
\treturn request({{
\t\turl: '{url}',
\t\tmethod: 'delete',
\t\tdata: ids,
\t}});
}};
"""


def gen_index(mod):
    name = mod['name']
    cn = mod['cn']
    perm = mod['perm']
    camel = name.replace('-', '')

    # Query form items
    query_items = ""
    for q in mod['query']:
        fname, flabel, ftype = q[0], q[1], q[2]
        if ftype == 'input':
            query_items += f"""\t\t\t\t<el-form-item :label="{flabel}" prop="{fname}">
\t\t\t\t\t<el-input :placeholder="请输入{flabel}" style="max-width: 180px" v-model="state.queryForm.{fname}" />
\t\t\t\t</el-form-item>\n"""
        elif ftype == 'select':
            opts = q[3]
            opt_str = ""
            for v, l in opts:
                opt_str += f'\t\t\t\t\t\t<el-option :key="{v}" :label="{l}" :value="{v}" />\n'
            query_items += f"""\t\t\t\t<el-form-item :label="{flabel}" prop="{fname}">
\t\t\t\t\t<el-select :placeholder="请选择{flabel}" class="w100" clearable v-model="state.queryForm.{fname}">
{opt_str}\t\t\t\t\t</el-select>
\t\t\t\t</el-form-item>\n"""
        elif ftype == 'daterange':
            query_items += f"""\t\t\t\t<el-form-item :label="{flabel}" prop="{fname}">
\t\t\t\t\t<el-date-picker start-placeholder="开始时间" end-placeholder="结束时间" range-separator="To" type="datetimerange" v-model="state.queryForm.{fname}" value-format="YYYY-MM-DD HH:mm:ss" />
\t\t\t\t</el-form-item>\n"""

    # Table columns
    col_items = '\t\t\t\t<el-table-column align="center" type="selection" width="40" />\n'
    col_items += '\t\t\t\t<el-table-column :label="序号" type="index" width="60" />\n'
    for c in mod['columns']:
        cname, clabel = c[0], c[1]
        extra = c[2] if len(c) > 2 else ''
        col_items += f'\t\t\t\t<el-table-column :label="{clabel}" prop="{cname}" show-overflow-tooltip {extra} />\n'

    # Query form default values
    query_defaults = ", ".join([f'{q[0]}: ""' for q in mod['query']])

    return f"""<template>
\t<div class="layout-padding">
\t\t<div class="layout-padding-auto layout-padding-view">
\t\t\t<el-row class="ml10" v-show="showSearch">
\t\t\t\t<el-form :inline="true" :model="state.queryForm" @keyup.enter="getDataList" ref="queryRef">
{query_items}\t\t\t\t\t<el-form-item>
\t\t\t\t\t\t<el-button @click="getDataList" icon="search" type="primary">查询</el-button>
\t\t\t\t\t\t<el-button @click="resetQuery" icon="Refresh">重置</el-button>
\t\t\t\t\t</el-form-item>
\t\t\t\t</el-form>
\t\t\t</el-row>
\t\t\t<el-row>
\t\t\t\t<div class="mb8" style="width: 100%">
\t\t\t\t\t<el-button @click="formDialogRef.openDialog()" class="ml10" icon="folder-add" type="primary" v-auth="'{perm}_add'">新增</el-button>
\t\t\t\t\t<el-button plain :disabled="multiple" @click="handleDelete(selectObjs)" class="ml10" icon="Delete" type="primary" v-auth="'{perm}_del'">删除</el-button>
\t\t\t\t\t<right-toolbar
\t\t\t\t\t\t:export="'{perm}_export'"
\t\t\t\t\t\t@exportExcel="exportExcel"
\t\t\t\t\t\t@queryTable="getDataList"
\t\t\t\t\t\tclass="ml10"
\t\t\t\t\t\tstyle="float: right; margin-right: 20px"
\t\t\t\t\t\tv-model:showSearch="showSearch"
\t\t\t\t\t></right-toolbar>
\t\t\t\t</div>
\t\t\t</el-row>
\t\t\t<el-table
\t\t\t\t:data="state.dataList"
\t\t\t\t@selection-change="handleSelectionChange"
\t\t\t\tv-loading="state.loading"
\t\t\t\tborder
\t\t\t\t:cell-style="tableStyle?.cellStyle"
\t\t\t\t:header-cell-style="tableStyle?.headerCellStyle"
\t\t\t>
{col_items}\t\t\t\t<el-table-column :label="操作" width="200">
\t\t\t\t\t<template #default="scope">
\t\t\t\t\t\t<el-button icon="edit-pen" @click="formDialogRef.openDialog(scope.row.id)" text type="primary" v-auth="'{perm}_edit'">编辑</el-button>
\t\t\t\t\t\t<el-button icon="delete" @click="handleDelete([scope.row.id])" text type="primary" v-auth="'{perm}_del'">删除</el-button>
\t\t\t\t\t</template>
\t\t\t\t</el-table-column>
\t\t\t</el-table>
\t\t\t<pagination @current-change="currentChangeHandle" @size-change="sizeChangeHandle" v-bind="state.pagination" />
\t\t</div>
\t\t<form-dialog @refresh="getDataList()" ref="formDialogRef" />
\t</div>
</template>

<script lang="ts" name="eh{camel}" setup>
import {{ BasicTableProps, useTable }} from '/@/hooks/table';
import {{ delObj, fetchList }} from '/@/api/eh/{name}';
import {{ useMessage, useMessageBox }} from '/@/hooks/message';

const FormDialog = defineAsyncComponent(() => import('./form.vue'));

const formDialogRef = ref();
const queryRef = ref();
const showSearch = ref(true);
const selectObjs = ref([]) as any;
const multiple = ref(true);

const state: BasicTableProps = reactive<BasicTableProps>({{
\tqueryForm: {{ {query_defaults} }},
\tpageList: fetchList,
}});

const {{ getDataList, currentChangeHandle, sizeChangeHandle, downBlobFile, tableStyle }} = useTable(state);

const resetQuery = () => {{
\tqueryRef.value?.resetFields();
\tgetDataList();
}};

const exportExcel = () => {{
\tdownBlobFile('{mod['url']}/export', state.queryForm, '{name}.xlsx');
}};

const handleSelectionChange = (objs: {{ id: string }}[]) => {{
\tselectObjs.value = objs.map(({{ id }}) => id);
\tmultiple.value = !objs.length;
}};

const handleDelete = async (ids: string[]) => {{
\ttry {{
\t\tawait useMessageBox().confirm('是否确认删除？');
\t}} catch {{
\t\treturn;
\t}}
\ttry {{
\t\tawait delObj(ids);
\t\tgetDataList();
\t\tuseMessage().success('删除成功');
\t}} catch (err: any) {{
\t\tuseMessage().error(err.msg);
\t}}
}};
</script>
"""


def gen_form(mod):
    name = mod['name']
    cn = mod['cn']
    perm = mod['perm']
    camel = name.replace('-', '')

    form_items = ""
    form_defaults = "id: '',\n"
    rules_items = ""

    for f in mod['form']:
        fname, flabel, ftype, required = f[0], f[1], f[2], f[3]
        form_defaults += f"\t\t{fname}: '',\n"

        if ftype == 'input':
            form_items += f'\t\t<el-form-item :label="{flabel}" prop="{fname}">\n\t\t\t<el-input v-model="form.{fname}" :placeholder="请输入{flabel}" />\n\t\t</el-form-item>\n'
        elif ftype == 'textarea':
            form_items += f'\t\t<el-form-item :label="{flabel}" prop="{fname}">\n\t\t\t<el-input type="textarea" rows="3" v-model="form.{fname}" :placeholder="请输入{flabel}" />\n\t\t</el-form-item>\n'
        elif ftype == 'number':
            form_items += f'\t\t<el-form-item :label="{flabel}" prop="{fname}">\n\t\t\t<el-input-number v-model="form.{fname}" :placeholder="请输入{flabel}" />\n\t\t</el-form-item>\n'
        elif ftype == 'datetime':
            form_items += f'\t\t<el-form-item :label="{flabel}" prop="{fname}">\n\t\t\t<el-date-picker type="datetime" v-model="form.{fname}" :placeholder="请选择{flabel}" value-format="YYYY-MM-DD HH:mm:ss" />\n\t\t</el-form-item>\n'

        if required:
            rules_items += f"\t{fname}: [{{ required: true, message: '{flabel}不能为空', trigger: 'blur' }}],\n"

    tpl = """<template>
\t<el-dialog :title="form.id ? '编辑' : '新增'" width="600" v-model="visible" :close-on-click-modal="false" draggable>
\t\t<el-form ref="dataFormRef" :model="form" :rules="dataRules" label-width="120px" v-loading="loading">
""" + form_items + """\t\t</el-form>
\t\t<template #footer>
\t\t\t<span class="dialog-footer">
\t\t\t\t<el-button @click="visible = false">取消</el-button>
\t\t\t\t<el-button type="primary" @click="onSubmit" :disabled="loading">确认</el-button>
\t\t\t</span>
\t\t</template>
\t</el-dialog>
</template>

<script setup lang="ts" name="eh""" + camel + """Dialog">
import { useMessage } from '/@/hooks/message';
import { getObj, addObj, putObj } from '/@/api/eh/""" + name + """';

const emit = defineEmits(['refresh']);

const dataFormRef = ref();
const visible = ref(false);
const loading = ref(false);

const form = reactive({
""" + form_defaults + """});

const dataRules = ref({
""" + rules_items + """});

const openDialog = (id: string) => {
\tvisible.value = true;
\tform.id = '';
\tnextTick(() => {
\t\tdataFormRef.value?.resetFields();
\t});
\tif (id) {
\t\tform.id = id;
\t\tgetObj(id).then((res: any) => {
\t\t\tObject.assign(form, res.data);
\t\t});
\t}
};

const onSubmit = async () => {
\tconst valid = await dataFormRef.value.validate().catch(() => {});
\tif (!valid) return false;
\ttry {
\t\tloading.value = true;
\t\tform.id ? await putObj(form) : await addObj(form);
\t\tuseMessage().success(form.id ? '编辑成功' : '新增成功');
\t\tvisible.value = false;
\t\temit('refresh');
\t} catch (err: any) {
\t\tuseMessage().error(err.msg);
\t} finally {
\t\tloading.value = false;
\t}
};

defineExpose({ openDialog });
</script>
"""
    return tpl


for mod in MODULES:
    name = mod['name']

    # API file
    api_dir = f"{BASE}/api/eh"
    os.makedirs(api_dir, exist_ok=True)
    with open(f'{api_dir}/{name}.ts', 'w', encoding='utf-8') as f:
        f.write(gen_api(mod))
    print(f"  Created {api_dir}/{name}.ts")

    # Vue index + form
    view_dir = f"{BASE}/views/eh/{name}"
    os.makedirs(view_dir, exist_ok=True)
    with open(f'{view_dir}/index.vue', 'w', encoding='utf-8') as f:
        f.write(gen_index(mod))
    print(f"  Created {view_dir}/index.vue")

    with open(f'{view_dir}/form.vue', 'w', encoding='utf-8') as f:
        f.write(gen_form(mod))
    print(f"  Created {view_dir}/form.vue")

print("\nDone! 6 modules x 3 files = 18 frontend files generated.")
